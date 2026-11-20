package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.WeaponDamage;
import net.per.primogemcraft.system.weapon.ViridescentHuntStats;

import java.util.UUID;

public final class SkywardHarpVortexEntity extends Entity {
    private static final TrackedData<Boolean> WIND_EYE = DataTracker.registerData(SkywardHarpVortexEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private UUID ownerId;
    private double radius;
    private long expires;
    private double damageRatio = 0.1D;

    public SkywardHarpVortexEntity(EntityType<? extends SkywardHarpVortexEntity> type, World level) {
        super(type, level);
        setNoGravity(true);
        noClip = true;
    }

    public static void spawn(ServerWorld level, PlayerEntity owner, Vec3d impact, double radius, int duration) {
        spawn(level, owner, impact.add(0.0D, 3.0D, 0.0D), radius, duration, 0.1D, false);
    }

    public static void spawnWindEye(ServerWorld level, PlayerEntity owner, Vec3d center, double damageRatio) {
        spawn(level, owner, center, ViridescentHuntStats.RADIUS, ViridescentHuntStats.DURATION, damageRatio, true);
    }

    private static void spawn(ServerWorld level, PlayerEntity owner, Vec3d center, double radius, int duration,
                              double damageRatio, boolean windEye) {
        var vortex = new SkywardHarpVortexEntity(PGCEntities.SKYWARD_HARP_VORTEX.get(), level);
        vortex.setPos(center.x, center.y, center.z);
        vortex.ownerId = owner.getUuid();
        vortex.radius = radius;
        vortex.expires = level.getTime() + duration;
        vortex.damageRatio = damageRatio;
        vortex.dataTracker.set(WIND_EYE, windEye);
        level.spawnEntity(vortex);
    }

    public boolean isWindEye() {
        return dataTracker.get(WIND_EYE);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient()) return;
        var owner = ownerId == null ? null : getWorld().getPlayerByUuid(ownerId);
        if (owner == null || !owner.isAlive() || owner.isSpectator() || getWorld().getTime() > expires) {
            discard();
            return;
        }
        var windEye = isWindEye();
        if (!windEye && age % 4 != 0) return;
        var damageTick = age % (windEye ? ViridescentHuntStats.DAMAGE_INTERVAL : 40) == 0;
        var center = getPos();
        for (var target : getWorld().getOtherEntities(this, new Box(center, center).expand(radius),
                candidate -> !windEye && (candidate instanceof ItemEntity || candidate instanceof ExperienceOrbEntity) && candidate.isAlive()
                        || candidate instanceof LivingEntity living && WishArrowEntity.canTarget(owner, living))) {
            var offset = center.subtract(target.getBoundingBox().getCenter());
            var distance = offset.length();
            if (distance > radius) continue;
            if (damageTick && target instanceof LivingEntity living)
                WeaponDamage.extraHit(living, WeaponDamage.continuous(getWorld(), Element.ANEMO, this, owner,
                        ElementDamageOptions.REACTING), (float) (owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * damageRatio));
            if (distance < 0.25D) continue;
            target.setVelocity(target.getVelocity().multiply(0.5D).add(offset.multiply(Math.min(0.35D, distance * 0.15D) / distance)));
            target.velocityModified = true;
        }
        if (getWorld().getTime() >= expires) discard();
    }

    /** 1.20.1 无此覆写点，保留为本类自有方法。 */
    public Box getBoundingBoxForCulling() {
        return new Box(getPos(), getPos()).expand(Math.sqrt(12.5D));
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(WIND_EYE, false);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound tag) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound tag) {
    }
}
