package net.per.primogemcraft.entity.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.zipline.ZiplineGrip;
import net.per.primogemcraft.system.zipline.ZiplineSpace;
import org.joml.Vector3f;

import java.util.List;

public class ZiplineCarrierEntity extends Entity {
    private static final TrackedData<BlockPos> SOURCE = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private static final TrackedData<BlockPos> TARGET = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private static final TrackedData<Boolean> MOVING = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> COMBAT = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Vector3f> SOURCE_OFFSET = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
    private static final TrackedData<Float> HEADING = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> SOURCE_ANCHOR = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TARGET_ANCHOR = DataTracker.registerData(ZiplineCarrierEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private double travelProgress;
    public static final double RANGE = 128;
    private static final int MIN_LIFT_HEIGHT = 16;
    private static final int MAX_LIFT_HEIGHT = 24;
    private static final int MIN_HORIZONTAL_OFFSET = 4;
    private static final int MAX_HORIZONTAL_OFFSET = 10;
    private int interpolationSteps;
    private Vec3d interpolationTarget = Vec3d.ZERO;
    private EntityPose previousPassengerPose;
    private ZiplineAnchorEntity lowerAnchor;
    private ZiplineAnchorEntity upperAnchor;
    private int departureDelay;
    private int arrivalDelay = -1;
    private float previousHeading;
    private float currentHeading;

    public ZiplineCarrierEntity(EntityType<?> type, World world) {
        super(type, world);
        noClip = true;
        setNoGravity(true);
    }

    public void initialize(ZiplineAnchorEntity anchor) {
        lowerAnchor = upperAnchor = anchor;
        dataTracker.set(SOURCE, anchor.basePosition());
        dataTracker.set(TARGET, anchor.basePosition());
        dataTracker.set(SOURCE_ANCHOR, anchor.getId());
        dataTracker.set(TARGET_ANCHOR, anchor.getId());
        updatePosition(anchor.attachment().x, anchor.attachment().y, anchor.attachment().z);
        setHeading(-anchor.getYaw() - 90.0F);
    }

    public float bodyYaw(float partialTick) {
        return MathHelper.lerpAngleDegrees(partialTick, previousHeading, currentHeading);
    }

    private void setHeading(float yaw) {
        dataTracker.set(HEADING, yaw);
        if (age < 2) previousHeading = currentHeading = yaw;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (HEADING.equals(data) && age < 2)
            previousHeading = currentHeading = dataTracker.get(HEADING);
    }

    public Vec3d source() {
        return endpointPosition(SOURCE, SOURCE_ANCHOR);
    }

    public Vec3d target() {
        return endpointPosition(TARGET, TARGET_ANCHOR);
    }

    private Vec3d endpointPosition(TrackedData<BlockPos> position, TrackedData<Integer> anchorId) {
        if (!combat() && getWorld().getEntityById(dataTracker.get(anchorId)) instanceof ZiplineAnchorEntity anchor)
            return anchor.attachment();
        var point = endpoint(dataTracker.get(position)).add(new Vec3d(dataTracker.get(SOURCE_OFFSET)));
        return combat() ? point : ZiplineSpace.pos(getWorld(), point);
    }

    public BlockPos sourceBase() {
        return getWorld().getEntityById(dataTracker.get(SOURCE_ANCHOR)) instanceof ZiplineAnchorEntity anchor
                ? anchor.basePosition() : dataTracker.get(SOURCE);
    }

    public BlockPos targetBase() {
        return getWorld().getEntityById(dataTracker.get(TARGET_ANCHOR)) instanceof ZiplineAnchorEntity anchor
                ? anchor.basePosition() : dataTracker.get(TARGET);
    }

    public boolean combat() {
        return dataTracker.get(COMBAT);
    }

    public static void lift(LivingEntity target) {
        var world = target.getWorld();
        if (world.isClient() || target.hasVehicle() || target.hasPassengers() || !target.isAlive()) return;
        var carrier = new ZiplineCarrierEntity(PGCEntities.ZIPLINE_CARRIER.get(), world);
        var origin = target.getPos().add(0, Math.max(ZiplineAnchorEntity.ANCHOR_HEIGHT, target.getHeight() + 0.5), 0);
        var destination = combatDestination(target, origin);
        if (destination == null) return;
        carrier.dataTracker.set(COMBAT, true);
        carrier.dataTracker.set(SOURCE, target.getBlockPos());
        carrier.dataTracker.set(TARGET, destination);
        carrier.dataTracker.set(SOURCE_OFFSET, origin.subtract(endpoint(target.getBlockPos())).toVector3f());
        carrier.updatePosition(origin.x, origin.y, origin.z);
        carrier.setHeading(ZiplineGrip.facingYaw(carrier.target().subtract(carrier.source())));
        carrier.lowerAnchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), world);
        carrier.upperAnchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), world);
        carrier.lowerAnchor.initializeTemporary(carrier, carrier.source());
        carrier.upperAnchor.initializeTemporary(carrier, carrier.target());
        carrier.lowerAnchor.setYaw(ZiplineGrip.modelYaw(carrier.currentHeading));
        carrier.upperAnchor.setYaw(ZiplineGrip.modelYaw(carrier.currentHeading) + 180.0F);
        if (!world.spawnEntity(carrier)) return;
        if (!world.spawnEntity(carrier.lowerAnchor) || !world.spawnEntity(carrier.upperAnchor)) {
            carrier.release();
            return;
        }
        if (target instanceof PlayerEntity player) player.stopFallFlying();
        target.stopUsingItem();
        if (!target.startRiding(carrier, true)) {
            carrier.release();
            return;
        }
        target.setOnGround(false);
        target.setVelocity(Vec3d.ZERO);
        carrier.departureDelay = 10;
        carrier.playStage(PGCSounds.ZIPLINE_PLACE.get());
        carrier.playStage(PGCSounds.ZIPLINE_ATTACH.get());
    }

    private static BlockPos combatDestination(LivingEntity target, Vec3d origin) {
        var world = target.getWorld();
        var random = target.getRandom();
        var maximumHeight = Math.min(MAX_LIFT_HEIGHT, (int) Math.floor(world.getTopY() - 2 - origin.y));
        if (maximumHeight < MIN_LIFT_HEIGHT) return null;
        for (var attempt = 0; attempt < 32; attempt++) {
            var x = random.nextInt(MAX_HORIZONTAL_OFFSET * 2 + 1) - MAX_HORIZONTAL_OFFSET;
            var z = random.nextInt(MAX_HORIZONTAL_OFFSET * 2 + 1) - MAX_HORIZONTAL_OFFSET;
            var distanceSquared = x * x + z * z;
            if (distanceSquared < MIN_HORIZONTAL_OFFSET * MIN_HORIZONTAL_OFFSET
                    || distanceSquared > MAX_HORIZONTAL_OFFSET * MAX_HORIZONTAL_OFFSET) continue;
            var height = MIN_LIFT_HEIGHT + random.nextInt(maximumHeight - MIN_LIFT_HEIGHT + 1);
            var end = origin.add(x, height, z);
            if (!world.getWorldBorder().contains(new Box(end, end).expand(2.2, 0, 2.2))) continue;
            var loaded = true;
            var steps = (int) Math.ceil(origin.distanceTo(end));
            for (var step = 0; step <= steps; step++) {
                var point = origin.lerp(end, (double) step / steps);
                if (!world.isChunkLoaded(BlockPos.ofFloored(point.add(-2.2, 0, -2.2)))
                        || !world.isChunkLoaded(BlockPos.ofFloored(point.add(-2.2, 0, 2.2)))
                        || !world.isChunkLoaded(BlockPos.ofFloored(point.add(2.2, 0, -2.2)))
                        || !world.isChunkLoaded(BlockPos.ofFloored(point.add(2.2, 0, 2.2)))) {
                    loaded = false;
                    break;
                }
            }
            if (loaded) return target.getBlockPos().add(x, height, z);
        }
        return null;
    }

    private Vec3d passengerGrip(Entity passenger) {
        return passenger instanceof PlayerEntity player ? ZiplineGrip.handOffset(false, 1.0F, currentHeading)
                : new Vec3d(0, passenger.getHeight() + 0.5, 0);
    }

    private void playStage(SoundEvent sound) {
        getWorld().playSound(null, getX(), getY(), getZ(), sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }

    private static Vec3d endpoint(BlockPos position) {
        return Vec3d.ofBottomCenter(position).add(0, ZiplineAnchorEntity.ANCHOR_HEIGHT, 0);
    }

    public boolean moving() {
        return dataTracker.get(MOVING);
    }

    public ZiplineAnchorEntity select(PlayerEntity player, List<ZiplineAnchorEntity> candidates) {
        if (moving() || combat()) return null;
        ZiplineAnchorEntity selected = null;
        var score = Math.cos(Math.toRadians(20));
        var eye = player.getEyePos();
        var look = player.getRotationVector();
        for (var anchor : candidates) {
            if (anchor.isRemoved() || anchor.temporary() || anchor.getId() == dataTracker.get(SOURCE_ANCHOR)) continue;
            var attachment = anchor.attachment();
            if (attachment.squaredDistanceTo(getPos()) > RANGE * RANGE) continue;
            var direction = attachment.subtract(eye).normalize();
            var alignment = direction.dotProduct(look);
            if (alignment > score) {
                score = alignment;
                selected = anchor;
            }
        }
        return selected;
    }

    public void depart(PlayerEntity player, int targetId) {
        if (moving() || combat() || !hasPassenger(player) || !(getWorld().getEntityById(targetId) instanceof ZiplineAnchorEntity anchor)) return;
        if (anchor.temporary() || anchor.getId() == dataTracker.get(SOURCE_ANCHOR) || !anchor.intact()
                || anchor.attachment().squaredDistanceTo(getPos()) > RANGE * RANGE
                || anchor.attachment().subtract(player.getEyePos()).normalize().dotProduct(player.getRotationVector()) < Math.cos(Math.toRadians(25))) return;
        upperAnchor = anchor;
        dataTracker.set(TARGET, anchor.basePosition());
        dataTracker.set(TARGET_ANCHOR, anchor.getId());
        travelProgress = 0;
        var direction = target().subtract(source());
        if (direction.horizontalLengthSquared() > 0.0001) setHeading(ZiplineGrip.facingYaw(direction));
        dataTracker.set(MOVING, true);
        playStage(PGCSounds.ZIPLINE_START.get());
    }

    public void release() {
        dataTracker.set(MOVING, false);
        for (var passenger : getPassengerList()) {
            var position = passenger.getPos();
            passenger.stopRiding();
            passenger.updatePosition(position.getX(), position.getY(), position.getZ());
            passenger.setVelocity(0, -0.08, 0);
            passenger.fallDistance = 0;
            passenger.setOnGround(false);
        }
        discard();
    }

    @Override
    public void kill() {
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED) return;
        super.remove(reason);
        if (!getWorld().isClient() && combat()) {
            if (lowerAnchor != null) lowerAnchor.discard();
            if (upperAnchor != null) upperAnchor.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        previousHeading = currentHeading;
        currentHeading = MathHelper.lerpAngleDegrees(0.25F, currentHeading, dataTracker.get(HEADING));
        if (getWorld().isClient()) {
            if (interpolationSteps > 0) {
                var next = getPos().lerp(interpolationTarget, 1.0 / interpolationSteps);
                updatePosition(next.x, next.y, next.z);
                interpolationSteps--;
            }
            return;
        }
        if (getPassengerList().isEmpty()) {
            discard();
            return;
        }
        var rider = getFirstPassenger();
        if (rider == null || !rider.isAlive() || rider instanceof PlayerEntity player && player.isSpectator()) {
            release();
            return;
        }
        if (combat() && (lowerAnchor == null || upperAnchor == null || lowerAnchor.isRemoved() || upperAnchor.isRemoved())) {
            release();
            return;
        }
        if (!combat()) {
            lowerAnchor = validateAnchor(dataTracker.get(SOURCE), lowerAnchor);
            upperAnchor = dataTracker.get(SOURCE_ANCHOR).equals(dataTracker.get(TARGET_ANCHOR)) ? lowerAnchor : validateAnchor(dataTracker.get(TARGET), upperAnchor);
            if (lowerAnchor == null || upperAnchor == null) {
                release();
                return;
            }
            dataTracker.set(SOURCE, lowerAnchor.basePosition());
            dataTracker.set(TARGET, upperAnchor.basePosition());
            dataTracker.set(SOURCE_ANCHOR, lowerAnchor.getId());
            dataTracker.set(TARGET_ANCHOR, upperAnchor.getId());
            if (!moving()) {
                var src = source();
                updatePosition(src.x, src.y, src.z);
            }
        }
        if (combat() && departureDelay > 0) {
            if (--departureDelay == 0) {
                dataTracker.set(MOVING, true);
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
            var start = source();
            var end = target();
            var direction = end.subtract(start);
            travelProgress = Math.min(1, travelProgress + speed / Math.max(direction.length(), 1.0E-8));
            var next = start.lerp(end, travelProgress);
            if (direction.horizontalLengthSquared() > 0.0001) setHeading(ZiplineGrip.facingYaw(direction));
            if (!getWorld().isChunkLoaded(BlockPos.ofFloored(next))) {
                release();
                return;
            }
            updatePosition(next.x, next.y, next.z);
            if (travelProgress >= 1) {
                updatePassengerPosition(rider);
                playStage(PGCSounds.ZIPLINE_ARRIVE.get());
                if (combat()) {
                    dataTracker.set(MOVING, false);
                    arrivalDelay = 20;
                    return;
                }
                dataTracker.set(SOURCE, dataTracker.get(TARGET));
                dataTracker.set(SOURCE_ANCHOR, dataTracker.get(TARGET_ANCHOR));
                lowerAnchor = upperAnchor;
                dataTracker.set(MOVING, false);
            }
        }
        rider.fallDistance = 0;
    }

    private ZiplineAnchorEntity validateAnchor(BlockPos position, ZiplineAnchorEntity cached) {
        var anchor = cached != null && !cached.isRemoved() ? cached : ZiplineAnchorEntity.find(getWorld(), position);
        return anchor != null && getWorld().isChunkLoaded(anchor.basePosition()) && anchor.intact() ? anchor : null;
    }

    @Override
    protected void updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater) {
        var position = getPos().subtract(passengerGrip(passenger));
        positionUpdater.accept(passenger, position.getX(), position.getY(), position.getZ());
        passenger.fallDistance = 0;
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof PlayerEntity player) {
            previousPassengerPose = player.getPose();
            player.setPose(EntityPose.STANDING);
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!getWorld().isClient()) playStage(PGCSounds.ZIPLINE_DETACH.get());
        if (passenger instanceof PlayerEntity player && player.getPose() == EntityPose.STANDING)
            player.setPose(previousPassengerPose);
        previousPassengerPose = null;
    }

    @Override
    public boolean canUsePortals() {
        return false;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return passenger.getPos();
    }

    @Override
    public boolean shouldDismountUnderwater() {
        return false;
    }

    @Override
    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        interpolationTarget = new Vec3d(x, y, z);
        interpolationSteps = Math.max(1, interpolationSteps);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(SOURCE_ANCHOR, -1);
        this.dataTracker.startTracking(TARGET_ANCHOR, -1);
        this.dataTracker.startTracking(SOURCE, BlockPos.ORIGIN);
        this.dataTracker.startTracking(TARGET, BlockPos.ORIGIN);
        this.dataTracker.startTracking(MOVING, false);
        this.dataTracker.startTracking(COMBAT, false);
        this.dataTracker.startTracking(SOURCE_OFFSET, new Vector3f());
        this.dataTracker.startTracking(HEADING, 0.0F);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }
}
