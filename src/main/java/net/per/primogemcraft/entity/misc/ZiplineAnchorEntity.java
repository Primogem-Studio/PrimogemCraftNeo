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
        // 已绑定到同一底座时直接返回：本方法由底座方块实体每刻调用，
        // 重复写方块状态与同步数据会造成大量区块更新与刷盘。
        var current = dataTracker.get(BASE);
        if (current.isPresent() && current.get().equals(base)) return;
        if (current.isEmpty()) {
            var facing = Direction.fromRotation(-getYaw());
            for (var index = 0; index < 9; index++) {
                var part = base.add(index % 3 - 1, 0, index / 3 - 1);
                var oldState = getWorld().getBlockState(part);
                if (oldState.isOf(PGCBlocks.ZIPLINE_BASE.get()) && oldState.get(ZiplineBaseBlock.FACING) != facing)
                    getWorld().setBlockState(part, oldState.with(ZiplineBaseBlock.FACING, facing), 2);
            }
        }
        dataTracker.set(BASE, Optional.of(base.toImmutable()));
        var state = getWorld().getBlockState(base);
        if (state.isOf(PGCBlocks.ZIPLINE_BASE.get())) setYaw(-state.get(ZiplineBaseBlock.FACING).asRotation());
        var pos = ZiplineSpace.pos(getWorld(), Vec3d.ofBottomCenter(base));
        updatePosition(pos.x, pos.y, pos.z);
        setBoundingBox(calculateBoundingBox());
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
        return !temporary() && super.shouldSave();
    }

    public static ZiplineAnchorEntity find(World world, BlockPos center) {
        // 与原版一致：只在底座正上方向 ANCHOR_HEIGHT 延伸的窄柱内查找。
        // 原先各方向扩张 8 格会扫到邻近滑索，且每次查找都要遍历大量实体。
        var origin = ZiplineSpace.pos(world, Vec3d.ofBottomCenter(center));
        var bounds = new Box(origin.x - 0.5, origin.y, origin.z - 0.5,
                origin.x + 0.5, origin.y + ANCHOR_HEIGHT, origin.z + 0.5);
        return world.getEntitiesByClass(ZiplineAnchorEntity.class, bounds,
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
        // 原版仅在服务端每秒校验底座完整性；包围盒在 bind 时确定，无需每刻重算。
        if (!getWorld().isClient() && age % 20 == 0 && !intact()) discard();
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (!getWorld().isClient()) attach(player);
        return ActionResult.success(getWorld().isClient());
    }

    /** 原版 isPickable()：能否被准星选中。Yarn 对应 canHit()，不是 isCollidable()。 */
    @Override
    public boolean canHit() {
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
