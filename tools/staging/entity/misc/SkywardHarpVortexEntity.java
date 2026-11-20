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
    private static final TrackedData<Boolean> WIND_EYE = DataTracker.defineId(SkywardHarpVortexEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private UUID ownerId;
    private double radius;
    private long expires;
    private double damageRatio = 0.1D;

    public SkywardHarpVortexEntity(EntityType<? extends SkywardHarpVortexEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public static void spawn(ServerWorld level, Player owner, Vec3d impact, double radius, int duration) {
        spawn(level, owner, impact.add(0.0D, 3.0D, 0.0D), radius, duration, 0.1D, false);
    }

    public static void spawnWindEye(ServerWorld level, Player owner, Vec3d center, double damageRatio) {
        spawn(level, owner, center, ViridescentHuntStats.RADIUS, ViridescentHuntStats.DURATION, damageRatio, true);
    }

    private static void spawn(ServerWorld level, Player owner, Vec3d center, double radius, int duration,
                              double damageRatio, boolean windEye) {
        var vortex = new SkywardHarpVortexEntity(PGCEntities.SKYWARD_HARP_VORTEX.get(), level);
        vortex.setPos(center);
        vortex.ownerId = owner.getUuid();
        vortex.radius = radius;
        vortex.expires = level.getTime() + duration;
        vortex.damageRatio = damageRatio;
        vortex.entityData.set(WIND_EYE, windEye);
        level.addFreshEntity(vortex);
    }

    public boolean isWindEye() {
        return entityData.get(WIND_EYE);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClient()) return;
        var owner = ownerId == null ? null : level().getPlayerByUUID(ownerId);
        if (owner == null || !owner.isAlive() || owner.isSpectator() || level().getTime() > expires) {
            discard();
            return;
        }
        var windEye = isWindEye();
        if (!windEye && tickCount % 4 != 0) return;
        var damageTick = tickCount % (windEye ? ViridescentHuntStats.DAMAGE_INTERVAL : 40) == 0;
        var center = position();
        for (var target : level().getEntities(this, new AABB(center, center).inflate(radius),
                candidate -> !windEye && (candidate instanceof ItemEntity || candidate instanceof ExperienceOrb) && candidate.isAlive()
                        || candidate instanceof LivingEntity living && WishArrowEntity.canTarget(owner, living))) {
            var offset = center.subtract(target.getBoundingBox().getCenter());
            var distance = offset.length();
            if (distance > radius) continue;
            if (damageTick && target instanceof LivingEntity living)
                WeaponDamage.extraHit(living, WeaponDamage.continuous(level(), Element.ANEMO, this, owner,
                        ElementDamageOptions.REACTING), (float) (owner.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * damageRatio));
            if (distance < 0.25D) continue;
            target.setDeltaMovement(target.getDeltaMovement().scale(0.5D).add(offset.scale(Math.min(0.35D, distance * 0.15D) / distance)));
            target.hurtMarked = true;
        }
        if (level().getTime() >= expires) discard();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(position(), position()).inflate(Math.sqrt(12.5D));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(WIND_EYE, false);
    }

    @Override
    protected void readAdditionalSaveData(NbtCompound tag) {
    }

    @Override
    protected void addAdditionalSaveData(NbtCompound tag) {
    }
}
