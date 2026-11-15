package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.entity.misc.FallingMoraPileEntity;

public class PackedMoraPileBlock extends FallingBlock {
    public PackedMoraPileBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        if (canFallThrough(level.getBlockState(pos.down())) && pos.getY() >= level.getBottomY()) {
            FallingMoraPileEntity.fall(level, pos, state);
        }
    }

    public void onPlaced(World level, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, net.minecraft.item.ItemStack stack) {
        super.onPlaced(level, pos, state, placer, stack);
        crushGlassBelow(level, pos);
    }

    @Override
    public void neighborUpdate(BlockState state, World level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborUpdate(state, level, pos, neighborBlock, fromPos, movedByPiston);
        crushGlassBelow(level, pos);
    }

    private static void crushGlassBelow(World level, BlockPos pos) {
        if (level.isClient()) return;
        var below = pos.down();
        var state = level.getBlockState(below);
        if (!state.isIn(net.minecraft.registry.tag.BlockTags.IMPERMEABLE)) return;
        level.syncWorldEvent(2001, below, Block.getRawIdFromState(state));
        level.setBlockState(below, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
    }
}
