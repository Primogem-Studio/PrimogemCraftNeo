package net.per.primogemcraft.entity.misc;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.block.ZiplineBaseBlock;
import net.per.primogemcraft.block.entity.ZiplineBaseBlockEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.zipline.ZiplineSpace;

import java.util.Optional;
import java.util.UUID;

public class ZiplineAnchorEntity extends Entity {
    private static final TrackedData<Boolean> TEMPORARY = DataTracker.registerData(ZiplineAnchorEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Optional<BlockPos>> BASE = DataTracker.registerData(ZiplineAnchorEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private UUID carrierId;
    public static final double MODEL_SCALE = 3.0;
    public static final double ANCHOR_HEIGHT = 28.0 / 16.0 * MODEL_SCALE;

    public ZiplineAnchorEntity(EntityType<?> type, World world) {
        super(type, world);
        setNoGravity(true);
        noClip = true;
    }

    public Vec3d attachment() {
        return temporary() ? getPos().add(0, ANCHOR_HEIGHT, 0)
                : ZiplineSpace.pos(getWorld(), Vec3d.ofBottomCenter(basePosition()).add(0, ANCHOR_HEIGHT, 0));
    }

    public BlockPos basePosition() {
        return dataTracker.get(BASE).orElse(getBlockPos());
    }

    public void bind(BlockPos base) {
        if (dataTracker.get(BASE).isEmpty()) {
            var facing = Direction.fromRotation(-getYaw());
            for (var index = 0; index < 9; index++) {
                var part = base.add(index % 3 - 1, 0, index / 3 - 1);
                var oldState = getWorld().getBlockState(part);
                if (oldState.isOf(PGCBlocks.ZIPLINE_BASE.get())) getWorld().setBlockState(part, oldState.with(ZiplineBaseBlock.FACING, facing), 2);
            }
        }
        dataTracker.set(BASE, Optional.of(base.toImmutable()));
        var state = getWorld().getBlockState(base);
        if (state.isOf(PGCBlocks.ZIPLINE_BASE.get())) setYaw(-state.get(ZiplineBaseBlock.FACING).asRotation());
        var pos = ZiplineSpace.pos(getWorld(), Vec3d.ofBottomCenter(base));
        updatePosition(pos.x, pos.y, pos.z);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (dataTracker == null || dataTracker.get(BASE).isEmpty())
            return new Box(getX() - 0.5, getY() + 1.05, getZ() - 0.5,
                    getX() + 0.5, getY() + 6.5, getZ() + 0.5);
        var local = Vec3d.ofBottomCenter(basePosition());
        var corner = ZiplineSpace.pos(getWorld(), local.add(-0.5, 1.05, -0.5));
        var bounds = new Box(corner, corner);
        for (var index = 1; index < 8; index++) {
            corner = ZiplineSpace.pos(getWorld(), local.add((index & 1) == 0 ? -0.5 : 0.5,
                    (index & 2) == 0 ? 1.05 : 6.5, (index & 4) == 0 ? -0.5 : 0.5));
            bounds = bounds.stretch(corner.x, corner.y, corner.z);
        }
        return bounds;
    }

    public boolean temporary() {
        return dataTracker.get(TEMPORARY);
    }

    public void initializeTemporary(ZiplineCarrierEntity carrier, Vec3d attachment) {
        dataTracker.set(TEMPORARY, true);
        carrierId = carrier.getUuid();
        var pos = attachment.add(0, -ANCHOR_HEIGHT, 0);
        updatePosition(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean shouldSave() {
        return !temporary() && dataTracker.get(BASE).isEmpty() && super.shouldSave();
    }

    public static ZiplineAnchorEntity find(World world, BlockPos center) {
        var pos = ZiplineSpace.pos(world, Vec3d.ofBottomCenter(center));
        return world.getEntitiesByClass(ZiplineAnchorEntity.class, new Box(pos, pos).expand(8),
                anchor -> !anchor.temporary() && anchor.basePosition().equals(center)).stream().findFirst().orElse(null);
    }

    public boolean intact() {
        if (temporary()) return !isRemoved();
        for (var index = 0; index < 9; index++) {
            var position = basePosition().add(index % 3 - 1, 0, index / 3 - 1);
            if (!getWorld().isChunkLoaded(position)) return true;
            var state = getWorld().getBlockState(position);
            if (!state.isOf(PGCBlocks.ZIPLINE_BASE.get()) || state.get(ZiplineBaseBlock.PART) != index) return false;
        }
        return true;
    }

    public void attach(PlayerEntity player) {
        if (temporary() || player.hasVehicle() || player.isSpectator() || !player.isAlive() || !intact()) return;
        var carrier = new ZiplineCarrierEntity(PGCEntities.ZIPLINE_CARRIER.get(), getWorld());
        carrier.initialize(this);
        if (!getWorld().spawnEntity(carrier)) return;
        player.stopFallFlying();
        player.stopUsingItem();
        if (!player.startRiding(carrier, true)) carrier.discard();
        else getWorld().playSound(null, player.getBlockPos(), PGCSounds.ZIPLINE_ATTACH.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (temporary()) {
            if (getWorld() instanceof ServerWorld server && (carrierId == null || !(server.getEntity(carrierId) instanceof ZiplineCarrierEntity carrier)
                    || carrier.isRemoved() || !carrier.hasPassengers())) discard();
            return;
        }
        if (getWorld().isClient()) {
            setBoundingBox(calculateBoundingBox());
        } else {
            var base = basePosition();
            if (getWorld().isChunkLoaded(base) && getWorld().getBlockState(base).isOf(PGCBlocks.ZIPLINE_BASE.get())) {
                if (!(getWorld().getBlockEntity(base) instanceof ZiplineBaseBlockEntity))
                    getWorld().setBlockState(base, getWorld().getBlockState(base)); getWorld().updateListeners(base, getWorld().getBlockState(base), getWorld().getBlockState(base), 3);
                if (getWorld().getBlockEntity(base) instanceof ZiplineBaseBlockEntity blockEntity) blockEntity.bind(this);
            }
            if (age % 20 == 0 && !intact()) discard();
        }
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!getWorld().isClient()) attach(player);
        return ActionResult.success(getWorld().isClient());
    }

    @Override
    public boolean isCollidable() {
        return !temporary();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public void kill() {
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED) return;
        super.remove(reason);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(TEMPORARY, false);
        this.dataTracker.startTracking(BASE, Optional.empty());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("Base")) dataTracker.set(BASE, Optional.of(BlockPos.fromLong(nbt.getLong("Base"))));
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putLong("Base", basePosition().asLong());
    }
}
