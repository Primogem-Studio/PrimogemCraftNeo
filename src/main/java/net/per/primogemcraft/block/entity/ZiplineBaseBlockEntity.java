package net.per.primogemcraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.block.ZiplineBaseBlock;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCBlockEntities;
import net.per.primogemcraft.registry.PGCEntities;

import java.util.UUID;

public class ZiplineBaseBlockEntity extends BlockEntity {
    private UUID anchorId;

    public ZiplineBaseBlockEntity(BlockPos position, BlockState state) {
        super(PGCBlockEntities.ZIPLINE_BASE.get(), position, state);
    }

    public void bind(ZiplineAnchorEntity anchor) {
        if (!anchor.getUUID().equals(anchorId)) {
            anchorId = anchor.getUUID();
            setChanged();
        }
        anchor.bind(worldPosition);
    }

    public static void tick(Level level, BlockPos position, BlockState state, ZiplineBaseBlockEntity base) {
        if (!(level instanceof ServerLevel server)) return;
        var existing = base.anchorId == null ? null : server.getEntity(base.anchorId);
        var anchor = existing instanceof ZiplineAnchorEntity found && !found.isRemoved()
                && (found.basePosition().equals(position) || level.hasChunkAt(found.basePosition())
                && !level.getBlockState(found.basePosition()).is(PGCBlocks.ZIPLINE_BASE))
                ? found : ZiplineAnchorEntity.find(level, position);
        if (anchor == null) {
            anchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
            anchor.setYRot(-state.getValue(ZiplineBaseBlock.FACING).toYRot());
            anchor.bind(position);
            if (!anchor.intact() || !level.addFreshEntity(anchor)) return;
        }
        base.bind(anchor);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        anchorId = tag.hasUUID("Anchor") ? tag.getUUID("Anchor") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (anchorId != null) tag.putUUID("Anchor", anchorId);
    }
}
