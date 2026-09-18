package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.system.event.EventGroup;
import net.per.primogemcraft.system.event.EventRegistry;

public class RandomEventEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_GROUP = SynchedEntityData.defineId(RandomEventEntity.class, EntityDataSerializers.INT);
    private static final double GRAVITY = 0.04D;
    private static final double FRICTION = 0.98D;
    private static final double BOUNCE = 0.5D;
    private static final float BOB_OFFSET_RANGE = (float) (Math.PI * 2.0);
    private static final float PICK_RADIUS = 1.5F;

    public final float bobOffs;

    public RandomEventEntity(EntityType<? extends RandomEventEntity> entityType, Level level) {
        super(entityType, level);
        bobOffs = random.nextFloat() * BOB_OFFSET_RANGE;
    }

    public void setGroup(int groupNumber) {
        entityData.set(DATA_GROUP, groupNumber);
    }

    public int groupNumber() {
        return entityData.get(DATA_GROUP);
    }

    public EventGroup group() {
        return EventRegistry.group(groupNumber());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount > PGCConfig.EVENT_ENTITY_LIFETIME.get()) {
            discard();
            return;
        }
        setDeltaMovement(getDeltaMovement().add(0.0D, -GRAVITY, 0.0D));
        move(MoverType.SELF, getDeltaMovement());
        var movement = getDeltaMovement();
        var vertical = onGround() ? -movement.y * BOUNCE : movement.y * FRICTION;
        setDeltaMovement(movement.x * FRICTION, vertical, movement.z * FRICTION);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        var group = group();
        discard();
        if (group != null && player instanceof ServerPlayer serverPlayer) EventRegistry.trigger(serverPlayer, group);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return PICK_RADIUS;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_GROUP, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
