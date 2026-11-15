package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.World;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.system.event.EventGroup;
import net.per.primogemcraft.system.event.EventRegistry;

public class RandomEventEntity extends Entity {
    private static final TrackedData<Integer> DATA_GROUP = DataTracker.registerData(RandomEventEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final double GRAVITY = 0.04D;
    private static final double FRICTION = 0.98D;
    private static final double BOUNCE = 0.5D;
    private static final float BOB_OFFSET_RANGE = (float) (Math.PI * 2.0);
    private static final float PICK_RADIUS = 1.5F;

    public final float bobOffs;

    public RandomEventEntity(EntityType<? extends RandomEventEntity> entityType, World level) {
        super(entityType, level);
        bobOffs = random.nextFloat() * BOB_OFFSET_RANGE;
    }

    public void setGroup(int groupNumber) {
        dataTracker.set(DATA_GROUP, groupNumber);
    }

    public int groupNumber() {
        return dataTracker.get(DATA_GROUP);
    }

    public EventGroup group() {
        return EventRegistry.group(groupNumber());
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient() && age > PGCConfig.EVENT_ENTITY_LIFETIME.get()) {
            discard();
            return;
        }
        setVelocity(getVelocity().add(0.0D, -GRAVITY, 0.0D));
        move(MovementType.SELF, getVelocity());
        var movement = getVelocity();
        var vertical = isOnGround() ? -movement.y * BOUNCE : movement.y * FRICTION;
        setVelocity(movement.x * FRICTION, vertical, movement.z * FRICTION);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (getWorld().isClient()) return ActionResult.SUCCESS;
        var group = group();
        discard();
        if (group != null && player instanceof ServerPlayerEntity serverPlayer) EventRegistry.trigger(serverPlayer, group);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canHit() {
        return true;
    }

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
    public boolean isImmuneToExplosion() {
        return true;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DATA_GROUP, 0);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
    }
}
