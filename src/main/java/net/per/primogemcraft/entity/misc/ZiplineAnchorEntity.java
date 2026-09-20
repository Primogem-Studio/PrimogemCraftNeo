package net.per.primogemcraft.entity.misc;

import net.minecraft.core.BlockPos;
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
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCSounds;

import java.util.UUID;

public class ZiplineAnchorEntity extends Entity {
    private static final EntityDataAccessor<Boolean> TEMPORARY = SynchedEntityData.defineId(ZiplineAnchorEntity.class, EntityDataSerializers.BOOLEAN);
    private UUID carrierId;
    public static final double MODEL_SCALE = 3.0;
    public static final double ANCHOR_HEIGHT = 28.0 / 16.0 * MODEL_SCALE;

    public ZiplineAnchorEntity(EntityType<?> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public Vec3 attachment() {
        return position().add(0, ANCHOR_HEIGHT, 0);
    }

    @Override
    protected AABB makeBoundingBox() {
        return new AABB(getX() - 0.5, getY() + 1.05, getZ() - 0.5,
                getX() + 0.5, getY() + 6.5, getZ() + 0.5);
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
        return !temporary() && super.shouldBeSaved();
    }

    public static ZiplineAnchorEntity find(Level level, BlockPos center) {
        return level.getEntitiesOfClass(ZiplineAnchorEntity.class, new AABB(center).expandTowards(0, ANCHOR_HEIGHT, 0),
                anchor -> !anchor.temporary() && anchor.blockPosition().equals(center)).stream().findFirst().orElse(null);
    }

    public boolean intact() {
        if (temporary()) return !isRemoved();
        for (var index = 0; index < 9; index++) {
            var position = blockPosition().offset(index % 3 - 1, 0, index / 3 - 1);
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
        if (!level().isClientSide() && tickCount % 20 == 0 && !intact()) discard();
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TEMPORARY, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
