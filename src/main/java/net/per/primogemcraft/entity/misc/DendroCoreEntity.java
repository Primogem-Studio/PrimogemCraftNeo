package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;

public class DendroCoreEntity extends Entity {
    private static final int LIFETIME_TICKS = 100;
    private static final double GRAVITY = 0.04D;
    private static final double DRAG = 0.98D;
    private static final double BOUNCE = 0.4D;
    private static final float BURNING_EXPLOSION_RADIUS = 4.0F;
    private static final float HIT_EXPLOSION_RADIUS = 2.0F;
    private static final String NBT_LIFETIME = "Lifetime";

    private int lifetime;

    public DendroCoreEntity(EntityType<? extends DendroCoreEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (isOnFire()) {
            if (!level().isClientSide()) {
                detonate(BURNING_EXPLOSION_RADIUS);
                discard();
            }
            return;
        }
        fall();
        if (level().isClientSide()) return;
        lifetime++;
        if (lifetime < LIFETIME_TICKS) return;
        dropLoot();
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide() || isRemoved() || isInvulnerableTo(source)) return false;
        if (source.getEntity() == this) return false;
        if (!isBurningDamage(source)) return false;
        detonate(HIT_EXPLOSION_RADIUS);
        discard();
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    private static boolean isBurningDamage(DamageSource source) {
        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)) return true;
        return ElementDamage.elementOf(source) == Element.PYRO;
    }

    private void detonate(float radius) {
        level().explode(this, getX(), getY(), getZ(), radius, Level.ExplosionInteraction.TNT);
    }

    private void fall() {
        setDeltaMovement(getDeltaMovement().add(0.0D, -GRAVITY, 0.0D));
        move(MoverType.SELF, getDeltaMovement());
        var movement = getDeltaMovement();
        var vertical = onGround() ? -movement.y * BOUNCE : movement.y * DRAG;
        setDeltaMovement(movement.x * DRAG, vertical, movement.z * DRAG);
    }

    private void dropLoot() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        var params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, serverLevel.damageSources().generic())
                .create(LootContextParamSets.ENTITY);
        var table = serverLevel.getServer().reloadableRegistries().getLootTable(getType().getDefaultLootTable());
        for (var stack : table.getRandomItems(params)) spawnAtLocation(stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        lifetime = compound.getInt(NBT_LIFETIME);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt(NBT_LIFETIME, lifetime);
    }
}
