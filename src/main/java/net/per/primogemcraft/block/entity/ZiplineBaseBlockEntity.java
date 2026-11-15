package net.per.primogemcraft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
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
        if (!anchor.getUuid().equals(anchorId)) {
            anchorId = anchor.getUuid();
            markDirty();
        }
        anchor.bind(pos);
    }

    public static void tick(World level, BlockPos position, BlockState state, ZiplineBaseBlockEntity base) {
        if (!(level instanceof ServerWorld server)) return;
        var existing = base.anchorId == null ? null : server.getEntity(base.anchorId);
        var anchor = existing instanceof ZiplineAnchorEntity found && !found.isRemoved()
                && (found.basePosition().equals(position) || level.isChunkLoaded(found.basePosition())
                && !level.getBlockState(found.basePosition()).isOf(PGCBlocks.ZIPLINE_BASE.get()))
                ? found : ZiplineAnchorEntity.find(level, position);
        if (anchor == null) {
            anchor = new ZiplineAnchorEntity(PGCEntities.ZIPLINE_ANCHOR.get(), level);
            anchor.setYaw(-state.get(ZiplineBaseBlock.FACING).asRotation());
            anchor.bind(position);
            if (!anchor.intact() || !level.spawnEntity(anchor)) return;
        }
        base.bind(anchor);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        anchorId = tag.containsUuid("Anchor") ? tag.getUuid("Anchor") : null;
    }

    @Override
    protected void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (anchorId != null) tag.putUuid("Anchor", anchorId);
    }
}
