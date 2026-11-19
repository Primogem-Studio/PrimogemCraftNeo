package net.per.primogemcraft.collab.tacz;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.SkyBulletPayload;
import net.per.primogemcraft.network.SkyTargetPayload;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;

public final class SkyTargeting {
    public static final int LOCK_TICKS = 8;
    private static final double RANGE = 64;
    private static final ResourceLocation GUN_ID = ResourceLocation.fromNamespaceAndPath("pgfs", "tiankong");
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.35F, 0.9F, 1), 0.7F);
    private static final Map<ServerPlayer, TargetLock> LOCKS = new WeakHashMap<>();
    private static final Map<EntityKineticBullet, LivingEntity> BULLETS = new WeakHashMap<>();

    private SkyTargeting() {
    }

    public static boolean isAiming(LivingEntity shooter) {
        var stack = shooter.getMainHandItem();
        var gun = IGun.getIGunOrNull(stack);
        return shooter.isAlive() && !shooter.isSpectator() && gun != null && GUN_ID.equals(gun.getGunId(stack))
                && IGunOperator.fromLivingEntity(shooter).getSynIsAiming();
    }

    private static boolean canTarget(LivingEntity shooter, LivingEntity target, double cone) {
        return target != shooter && target.isAlive() && !target.isSpectator() && target.isPickable()
                && !(target instanceof ArmorStand) && !target.isAlliedTo(shooter) && !shooter.isAlliedTo(target)
                && !(target instanceof Player player && (player.isCreative()
                || shooter instanceof Player attacker && !attacker.canHarmPlayer(player)))
                && target.level() == shooter.level() && shooter.distanceToSqr(target) <= RANGE * RANGE
                && alignment(shooter, target) >= cone && shooter.hasLineOfSight(target);
    }

    private static double alignment(LivingEntity shooter, LivingEntity target) {
        return shooter.getLookAngle().dot(target.getBoundingBox().getCenter().subtract(shooter.getEyePosition()).normalize());
    }

    @SubscribeEvent
    public static void tickPlayer(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        var previous = LOCKS.get(player);
        if (!isAiming(player)) {
            if (LOCKS.remove(player) != null) PayloadRegistrar.sendToPlayer(player, new SkyTargetPayload(-1, 0));
            return;
        }
        var target = previous != null && canTarget(player, previous.target(), 0.9063) ? previous.target()
                : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RANGE),
                        candidate -> canTarget(player, candidate, 0.9781)).stream()
                        .max(Comparator.comparingDouble(candidate -> alignment(player, candidate))).orElse(null);
        if (target == null) {
            if (LOCKS.remove(player) != null) PayloadRegistrar.sendToPlayer(player, new SkyTargetPayload(-1, 0));
            return;
        }
        var ticks = previous != null && previous.target() == target ? Math.min(LOCK_TICKS, previous.ticks() + 1) : 1;
        LOCKS.put(player, new TargetLock(target, ticks));
        if (previous == null || previous.target() != target || previous.ticks() != ticks)
            PayloadRegistrar.sendToPlayer(player, new SkyTargetPayload(target.getId(), ticks));
    }

    @SubscribeEvent
    public static void bulletSpawned(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof EntityKineticBullet bullet)
                || !GUN_ID.equals(bullet.getGunId()) || !(bullet.getOwner() instanceof ServerPlayer player)) return;
        var lock = LOCKS.get(player);
        if (lock == null || lock.ticks() < LOCK_TICKS || !isAiming(player) || !canTarget(player, lock.target(), 0.9063)) return;
        BULLETS.put(bullet, lock.target());
        guide(bullet, lock.target());
    }

    @SubscribeEvent
    public static void bulletHit(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide().isServer() && event.getBullet() instanceof EntityKineticBullet bullet)
            BULLETS.remove(bullet);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) LOCKS.remove(player);
    }

    @SubscribeEvent
    public static void tickLevel(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()
                || !(event.level instanceof ServerLevel level) || BULLETS.isEmpty()) return;
        var iterator = BULLETS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var bullet = entry.getKey();
            var target = entry.getValue();
            if (bullet.level() != level || bullet.isRemoved()) {
                iterator.remove();
                continue;
            }
            if (!(bullet.getOwner() instanceof LivingEntity shooter) || !target.isAlive() || target.level() != level
                    || target.isAlliedTo(shooter) || shooter.isAlliedTo(target) || target.isSpectator()
                    || target instanceof Player player && (player.isCreative()
                    || shooter instanceof Player attacker && !attacker.canHarmPlayer(player))
                    || level.clip(new ClipContext(bullet.position(), target.getBoundingBox().getCenter(),
                            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, bullet)).getType() != HitResult.Type.MISS
                    || bullet.distanceToSqr(target) > RANGE * RANGE || bullet.tickCount > 40) {
                iterator.remove();
                continue;
            }
            guide(bullet, target);
            var velocity = bullet.getDeltaMovement();
            PayloadRegistrar.sendToPlayersTrackingEntity(bullet, new SkyBulletPayload(bullet.getId(), bullet.position(), velocity));
            for (var index = 0; index < 5; index++) {
                var point = bullet.position().add(velocity.scale(index / 5.0));
                level.sendParticles(TRAIL, point.x, point.y, point.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void guide(EntityKineticBullet bullet, LivingEntity target) {
        var speed = bullet.getDeltaMovement().length();
        if (speed < 0.001) return;
        var center = target.getBoundingBox().getCenter();
        var flight = Math.min(2, bullet.position().distanceTo(center) / speed);
        var direction = center.add(target.getDeltaMovement().scale(flight)).subtract(bullet.position());
        if (direction.lengthSqr() > 0.001) bullet.setDeltaMovement(direction.normalize().scale(speed));
    }

    private record TargetLock(LivingEntity target, int ticks) {
    }
}
