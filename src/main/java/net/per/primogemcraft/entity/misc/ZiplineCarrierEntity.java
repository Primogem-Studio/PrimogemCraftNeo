package net.per.primogemcraft.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.zipline.ZiplineGrip;
import org.joml.Vector3f;

import java.util.List;

public class ZiplineCarrierEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> SOURCE = SynchedEntityData.defineId(ZiplineCarrierEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> TARGET = SynchedEntityData.defineId(ZiplineCarrierEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> MOVING = SynchedEntityData.defineId(ZiplineCarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> COMBAT = SynchedEntityData.defineId(ZiplineCarrierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> SOURCE_OFFSET = SynchedEntityData.defineId(ZiplineCarrierEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Float> HEADING = SynchedEntityData.defineId(ZiplineCarrierEntity.class, EntityDataSerializers.FLOAT);
    public static final double RANGE = 128;
    private static final int MIN_LIFT_HEIGHT = 16;
    private static final int MAX_LIFT_HEIGHT = 24;
    private static final int MIN_HORIZONTAL_OFFSET = 4;
    private static final int MAX_HORIZONTAL_OFFSET = 10;
    private int interpolationSteps;
    private Vec3 interpolationTarget = Vec3.ZERO;
    private Pose previousPassengerPose;
    private ZiplineAnchorEntity lowerAnchor;
    private ZiplineAnchorEntity upperAnchor;
    private int departureDelay;
    private int arrivalDelay = -1;
    private float previousHeading;
    private float currentHeading;

    public ZiplineCarrierEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void initialize(ZiplineAnchorEntity anchor) {
        lowerAnchor = upperAnchor = anchor;
        entityData.set(SOURCE, anchor.blockPosition());
        entityData.set(TARGET, anchor.blockPosition());
        upperAnchor = anchor;
        setPos(anchor.attachment());
        setHeading(-anchor.getYRot() - 90.0F);
    }

    public float bodyYaw(float partialTick) {
        return Mth.rotLerp(partialTick, previousHeading, currentHeading);
    }

    private void setHeading(float yaw) {
        entityData.set(HEADING, yaw);
        if (tickCount < 2) previousHeading = currentHeading = yaw;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (HEADING.equals(accessor) && tickCount < 2)
            previousHeading = currentHeading = entityData.get(HEADING);
    }

    public Vec3 source() {
        return endpoint(entityData.get(SOURCE)).add(new Vec3(entityData.get(SOURCE_OFFSET)));
    }

    public Vec3 target() {
        return endpoint(entityData.get(TARGET)).add(new Vec3(entityData.get(SOURCE_OFFSET)));
    }

    public boolean combat() {
        return entityData.get(COMBAT);
    }

    public static void lift(LivingEntity target) {
        var level = target.level();
        if (level.isClientSide() || target.isPassenger() || target.isVehicle() || !target.isAlive()) return;
        var carrier = new ZiplineCarrierEntity(PGCEntities.ZIPLINE_CARRIER.get(), level);
        var origin = target.position().add(0, Math.max(ZiplineAnchorEntity.ANCHOR_HEIGHT, target.getBbHeight() + 0.5), 0);
        var destination = combatDestination(target, origin);
        if (destination == null) return;
        carrier.entityData.set(COMBAT, true);
        carrier.entityData.set(SOURCE, target.blockPosition());
        carrier.entityData.set(TARGET, destination);
        carrier.entityData.set(SOURCE_OFFSET, origin.subtract(endpoint(target.blockPosition())).toVector3f());
        carrier.setPos(origin);
        carrier.setHeading(ZiplineGrip.facingYaw(carrier.target().subtract(carrier.source())));
        carrier.lowerAnchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
        carrier.upperAnchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
        carrier.lowerAnchor.initializeTemporary(carrier, carrier.source());
        carrier.upperAnchor.initializeTemporary(carrier, carrier.target());
        carrier.lowerAnchor.setYRot(ZiplineGrip.modelYaw(carrier.currentHeading));
        carrier.upperAnchor.setYRot(ZiplineGrip.modelYaw(carrier.currentHeading) + 180.0F);
        if (!level.addFreshEntity(carrier)) return;
        if (!level.addFreshEntity(carrier.lowerAnchor) || !level.addFreshEntity(carrier.upperAnchor)) {
            carrier.release();
            return;
        }
        if (target instanceof Player player) player.stopFallFlying();
        target.stopUsingItem();
        if (!target.startRiding(carrier, true)) {
            carrier.release();
            return;
        }
        target.setOnGround(false);
        target.setDeltaMovement(Vec3.ZERO);
        carrier.departureDelay = 10;
        carrier.playStage(PGCSounds.ZIPLINE_PLACE.get());
        carrier.playStage(PGCSounds.ZIPLINE_ATTACH.get());
    }

    private static BlockPos combatDestination(LivingEntity target, Vec3 origin) {
        var level = target.level();
        var random = target.getRandom();
        var maximumHeight = Math.min(MAX_LIFT_HEIGHT, (int) Math.floor(level.getMaxBuildHeight() - 2 - origin.y));
        if (maximumHeight < MIN_LIFT_HEIGHT) return null;
        for (var attempt = 0; attempt < 32; attempt++) {
            var x = random.nextInt(MAX_HORIZONTAL_OFFSET * 2 + 1) - MAX_HORIZONTAL_OFFSET;
            var z = random.nextInt(MAX_HORIZONTAL_OFFSET * 2 + 1) - MAX_HORIZONTAL_OFFSET;
            var distanceSquared = x * x + z * z;
            if (distanceSquared < MIN_HORIZONTAL_OFFSET * MIN_HORIZONTAL_OFFSET
                    || distanceSquared > MAX_HORIZONTAL_OFFSET * MAX_HORIZONTAL_OFFSET) continue;
            var height = MIN_LIFT_HEIGHT + random.nextInt(maximumHeight - MIN_LIFT_HEIGHT + 1);
            var end = origin.add(x, height, z);
            if (!level.getWorldBorder().isWithinBounds(new AABB(end, end).inflate(2.2, 0, 2.2))) continue;
            var loaded = true;
            var steps = (int) Math.ceil(origin.distanceTo(end));
            for (var step = 0; step <= steps; step++) {
                var point = origin.lerp(end, (double) step / steps);
                if (!level.hasChunkAt(BlockPos.containing(point.add(-2.2, 0, -2.2)))
                        || !level.hasChunkAt(BlockPos.containing(point.add(-2.2, 0, 2.2)))
                        || !level.hasChunkAt(BlockPos.containing(point.add(2.2, 0, -2.2)))
                        || !level.hasChunkAt(BlockPos.containing(point.add(2.2, 0, 2.2)))) {
                    loaded = false;
                    break;
                }
            }
            if (loaded) return target.blockPosition().offset(x, height, z);
        }
        return null;
    }

    private Vec3 passengerGrip(Entity passenger) {
        return passenger instanceof Player player ? ZiplineGrip.handOffset(false, player.getScale(), currentHeading)
                : new Vec3(0, passenger.getBbHeight() + 0.5, 0);
    }

    private void playStage(SoundEvent sound) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static Vec3 endpoint(BlockPos position) {
        return Vec3.atBottomCenterOf(position).add(0, ZiplineAnchorEntity.ANCHOR_HEIGHT, 0);
    }

    public boolean moving() {
        return entityData.get(MOVING);
    }

    public ZiplineAnchorEntity select(Player player, List<ZiplineAnchorEntity> candidates) {
        if (moving() || combat()) return null;
        ZiplineAnchorEntity selected = null;
        var score = Math.cos(Math.toRadians(20));
        var eye = player.getEyePosition();
        var look = player.getLookAngle();
        for (var anchor : candidates) {
            if (anchor.isRemoved() || anchor.temporary() || anchor.blockPosition().equals(entityData.get(SOURCE))) continue;
            var attachment = anchor.attachment();
            if (attachment.distanceToSqr(position()) > RANGE * RANGE) continue;
            var direction = attachment.subtract(eye).normalize();
            var alignment = direction.dot(look);
            if (alignment > score) {
                score = alignment;
                selected = anchor;
            }
        }
        return selected;
    }

    public void depart(Player player, int targetId) {
        if (moving() || combat() || !hasPassenger(player) || !(level().getEntity(targetId) instanceof ZiplineAnchorEntity anchor)) return;
        if (anchor.temporary() || anchor.blockPosition().equals(entityData.get(SOURCE)) || !anchor.intact()
                || anchor.attachment().distanceToSqr(position()) > RANGE * RANGE
                || anchor.attachment().subtract(player.getEyePosition()).normalize().dot(player.getLookAngle()) < Math.cos(Math.toRadians(25))) return;
        entityData.set(TARGET, anchor.blockPosition());
        var direction = target().subtract(source());
        if (direction.horizontalDistanceSqr() > 0.0001) setHeading(ZiplineGrip.facingYaw(direction));
        entityData.set(MOVING, true);
        playStage(PGCSounds.ZIPLINE_START.get());
    }

    public void release() {
        entityData.set(MOVING, false);
        for (var passenger : getPassengers()) {
            var position = passenger.position();
            passenger.stopRiding();
            passenger.setPos(position);
            passenger.setDeltaMovement(0, -0.08, 0);
            passenger.fallDistance = 0;
            passenger.setOnGround(false);
        }
        discard();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!level().isClientSide() && combat()) {
            if (lowerAnchor != null) lowerAnchor.discard();
            if (upperAnchor != null) upperAnchor.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        previousHeading = currentHeading;
        currentHeading = Mth.rotLerp(0.25F, currentHeading, entityData.get(HEADING));
        if (level().isClientSide()) {
            if (interpolationSteps > 0) {
                setPos(position().lerp(interpolationTarget, 1.0 / interpolationSteps));
                interpolationSteps--;
            }
            return;
        }
        if (getPassengers().isEmpty()) {
            discard();
            return;
        }
        var rider = getFirstPassenger();
        if (rider == null || !rider.isAlive() || rider instanceof Player player && player.isSpectator()) {
            release();
            return;
        }
        if (combat() && (lowerAnchor == null || upperAnchor == null || lowerAnchor.isRemoved() || upperAnchor.isRemoved())) {
            release();
            return;
        }
        if (!combat()) {
            lowerAnchor = validateAnchor(entityData.get(SOURCE), lowerAnchor);
            upperAnchor = entityData.get(SOURCE).equals(entityData.get(TARGET)) ? lowerAnchor : validateAnchor(entityData.get(TARGET), upperAnchor);
            if (lowerAnchor == null || upperAnchor == null) {
                release();
                return;
            }
        }
        if (combat() && departureDelay > 0) {
            if (--departureDelay == 0) {
                entityData.set(MOVING, true);
                playStage(PGCSounds.ZIPLINE_START.get());
            }
            return;
        }
        if (combat() && arrivalDelay >= 0) {
            if (--arrivalDelay <= 0) release();
            return;
        }
        if (moving()) {
            var speed = PGCConfig.ZIPLINE_SPEED.get() / (combat() ? 40.0 : 20.0);
            var remaining = target().subtract(position());
            var next = remaining.length() <= speed ? target() : position().add(remaining.normalize().scale(speed));
            if (!level().hasChunkAt(BlockPos.containing(next))) {
                release();
                return;
            }
            setPos(next);
            if (remaining.length() <= speed) {
                positionRider(rider);
                playStage(PGCSounds.ZIPLINE_ARRIVE.get());
                if (combat()) {
                    entityData.set(MOVING, false);
                    arrivalDelay = 20;
                    return;
                }
                entityData.set(SOURCE, entityData.get(TARGET));
                lowerAnchor = upperAnchor;
                entityData.set(MOVING, false);
            }
        }
        rider.fallDistance = 0;
    }

    private ZiplineAnchorEntity validateAnchor(BlockPos position, ZiplineAnchorEntity cached) {
        if (!level().hasChunkAt(position)) return null;
        var anchor = cached != null && !cached.isRemoved() && cached.blockPosition().equals(position)
                ? cached : ZiplineAnchorEntity.find(level(), position);
        return anchor != null && anchor.intact() ? anchor : null;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction move) {
        var position = position().subtract(passengerGrip(passenger));
        move.accept(passenger, position.x, position.y, position.z);
        passenger.fallDistance = 0;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof Player player) {
            previousPassengerPose = player.getForcedPose();
            player.setForcedPose(Pose.STANDING);
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!level().isClientSide()) playStage(PGCSounds.ZIPLINE_DETACH.get());
        if (passenger instanceof Player player && player.getForcedPose() == Pose.STANDING)
            player.setForcedPose(previousPassengerPose);
        previousPassengerPose = null;
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return passenger.position();
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        interpolationTarget = new Vec3(x, y, z);
        interpolationSteps = Math.max(1, steps);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SOURCE, BlockPos.ZERO);
        builder.define(TARGET, BlockPos.ZERO);
        builder.define(MOVING, false);
        builder.define(COMBAT, false);
        builder.define(SOURCE_OFFSET, new Vector3f());
        builder.define(HEADING, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
