package net.per.primogemcraft.entity.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.block.ZiplineBaseBlock;
import net.per.primogemcraft.block.entity.ZiplineBaseBlockEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCSounds;

import net.per.primogemcraft.system.zipline.ZiplineSpace;

import java.util.Optional;
import java.util.UUID;

public class ZiplineAnchorEntity extends Entity {
    private static final EntityDataAccessor<Boolean> TEMPORARY = SynchedEntityData.defineId(ZiplineAnchorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> BASE = SynchedEntityData.defineId(ZiplineAnchorEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private UUID carrierId;
    public static final double MODEL_SCALE = 3.0;
    public static final double ANCHOR_HEIGHT = 28.0 / 16.0 * MODEL_SCALE;

    public ZiplineAnchorEntity(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public Vec3 attachment() {
        return temporary() ? position().add(0, ANCHOR_HEIGHT, 0)
                : ZiplineSpace.worldPosition(level(), Vec3.atBottomCenterOf(basePosition()).add(0, ANCHOR_HEIGHT, 0));
    }

    public BlockPos basePosition() {
        return entityData.get(BASE).orElse(blockPosition());
    }

    public void bind(BlockPos base) {
        if (entityData.get(BASE).isEmpty()) {
            var facing = Direction.fromYRot(-getYRot());
            for (var index = 0; index < 9; index++) {
                var part = base.offset(index % 3 - 1, 0, index / 3 - 1);
                var oldState = level().getBlockState(part);
                if (oldState.is(PGCBlocks.ZIPLINE_BASE)) level().setBlock(part, oldState.setValue(ZiplineBaseBlock.FACING, facing), 2);
            }
        }
        entityData.set(BASE, Optional.of(base.immutable()));
        var state = level().getBlockState(base);
        if (state.is(PGCBlocks.ZIPLINE_BASE)) setYRot(-state.getValue(ZiplineBaseBlock.FACING).toYRot());
        setPos(ZiplineSpace.worldPosition(level(), Vec3.atBottomCenterOf(base)));
    }

    @Override
    protected AABB makeBoundingBox() {
        if (entityData == null || entityData.get(BASE).isEmpty())
            return new AABB(getX() - 0.5, getY() + 1.05, getZ() - 0.5,
                    getX() + 0.5, getY() + 6.5, getZ() + 0.5);
        var local = Vec3.atBottomCenterOf(basePosition());
        var corner = ZiplineSpace.worldPosition(level(), local.add(-0.5, 1.05, -0.5));
        var bounds = new AABB(corner, corner);
        for (var index = 1; index < 8; index++) {
            corner = ZiplineSpace.worldPosition(level(), local.add((index & 1) == 0 ? -0.5 : 0.5,
                    (index & 2) == 0 ? 1.05 : 6.5, (index & 4) == 0 ? -0.5 : 0.5));
            bounds = bounds.minmax(new AABB(corner, corner));
        }
        return bounds;
    }

    public boolean temporary() {
        return entityData.get(TEMPORARY);
    }

    public void initializeTemporary(ZiplineCarrierEntity carrier, Vec3 attachment) {
        entityData.set(TEMPORARY, true);
        carrierId = carrier.getUUID();
        setPos(attachment.add(0, -ANCHOR_HEIGHT, 0));
    }

    @Override
    public boolean shouldBeSaved() {
        return !temporary() && entityData.get(BASE).isEmpty() && super.shouldBeSaved();
    }

    public static ZiplineAnchorEntity find(Level level, BlockPos center) {
        var world = ZiplineSpace.worldPosition(level, Vec3.atBottomCenterOf(center));
        return level.getEntitiesOfClass(ZiplineAnchorEntity.class, new AABB(world, world).inflate(8),
                anchor -> !anchor.temporary() && anchor.basePosition().equals(center)).stream().findFirst().orElse(null);
    }

    public boolean intact() {
        if (temporary()) return !isRemoved();
        for (var index = 0; index < 9; index++) {
            var position = basePosition().offset(index % 3 - 1, 0, index / 3 - 1);
            if (!level().hasChunkAt(position)) return true;
            var state = level().getBlockState(position);
            if (!state.is(PGCBlocks.ZIPLINE_BASE) || state.getValue(ZiplineBaseBlock.PART) != index) return false;
        }
        return true;
    }

    public void attach(Player player) {
        if (temporary() || player.isPassenger() || player.isSpectator() || !player.isAlive() || !intact()) return;
        var carrier = new ZiplineCarrierEntity(PGCEntities.ZIPLINE_CARRIER.get(), level());
        carrier.initialize(this);
        if (!level().addFreshEntity(carrier)) return;
        player.stopFallFlying();
        player.stopUsingItem();
        if (!player.startRiding(carrier, true)) carrier.discard();
        else level().playSound(null, player.blockPosition(), PGCSounds.ZIPLINE_ATTACH.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (temporary()) {
            if (level() instanceof ServerLevel server && (carrierId == null || !(server.getEntity(carrierId) instanceof ZiplineCarrierEntity carrier)
                    || carrier.isRemoved() || !carrier.isVehicle())) discard();
            return;
        }
        if (level().isClientSide()) {
            setBoundingBox(makeBoundingBox());
        } else {
            var base = basePosition();
            if (level().hasChunkAt(base) && level().getBlockState(base).is(PGCBlocks.ZIPLINE_BASE)) {
                if (!(level().getBlockEntity(base) instanceof ZiplineBaseBlockEntity))
                    level().setBlockEntity(new ZiplineBaseBlockEntity(base, level().getBlockState(base)));
                if (level().getBlockEntity(base) instanceof ZiplineBaseBlockEntity blockEntity) blockEntity.bind(this);
            }
            if (tickCount % 20 == 0 && !intact()) discard();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide()) attach(player);
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public boolean isPickable() {
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TEMPORARY, false);
        builder.define(BASE, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Base")) entityData.set(BASE, Optional.of(BlockPos.of(tag.getLong("Base"))));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("Base", basePosition().asLong());
    }
}
