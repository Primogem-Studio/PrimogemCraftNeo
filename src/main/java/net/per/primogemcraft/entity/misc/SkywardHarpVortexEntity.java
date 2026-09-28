package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.WeaponDamage;

import java.util.UUID;

public final class SkywardHarpVortexEntity extends Entity {
    private UUID ownerId;
    private double radius;
    private long expires;

    public SkywardHarpVortexEntity(EntityType<? extends SkywardHarpVortexEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public static void spawn(ServerLevel level, Player owner, Vec3 impact, double radius, int duration) {
        var vortex = new SkywardHarpVortexEntity(PGCEntities.SKYWARD_HARP_VORTEX.get(), level);
        vortex.setPos(impact.add(0.0D, 3.0D, 0.0D));
        vortex.ownerId = owner.getUUID();
        vortex.radius = radius;
        vortex.expires = level.getGameTime() + duration;
        level.addFreshEntity(vortex);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        var owner = ownerId == null ? null : level().getPlayerByUUID(ownerId);
        if (owner == null || !owner.isAlive() || owner.isSpectator() || level().getGameTime() > expires) {
            discard();
            return;
        }
        if (tickCount % 4 != 0) return;
        var center = position();
        for (var target : level().getEntities(this, new AABB(center, center).inflate(radius),
                candidate -> (candidate instanceof ItemEntity || candidate instanceof ExperienceOrb) && candidate.isAlive()
                        || candidate instanceof LivingEntity living && WishArrowEntity.canTarget(owner, living))) {
            var offset = center.subtract(target.getBoundingBox().getCenter());
            var distance = offset.length();
            if (distance > radius) continue;
            if (tickCount % 40 == 0 && target instanceof LivingEntity living)
                WeaponDamage.extraHit(living, WeaponDamage.continuous(level(), Element.ANEMO, this, owner,
                        ElementDamageOptions.REACTING), (float) (owner.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.1D));
            if (distance < 0.25D) continue;
            target.setDeltaMovement(target.getDeltaMovement().scale(0.5D).add(offset.scale(Math.min(0.35D, distance * 0.15D) / distance)));
            target.hurtMarked = true;
        }
        if (level().getGameTime() >= expires) discard();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return new AABB(position(), position()).inflate(Math.sqrt(12.5D));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
