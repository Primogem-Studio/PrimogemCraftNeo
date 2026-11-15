package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.World;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.shape.VoxelShape;

public class NetherrackFarmlandBlock extends PGCShapeBlock {
    public NetherrackFarmlandBlock(AbstractBlock.Settings settings, VoxelShape shape) {
        super(settings, shape);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        if (world.isClient()) return;
        var above = world.getBlockState(pos.up());
        if (above.isAir() || above.isIn(BlockTags.CROPS) || above.isOf(Blocks.WATER) || above.isOf(Blocks.LAVA)) return;
        world.setBlockState(pos, Blocks.NETHERRACK.getDefaultState(), Block.NOTIFY_ALL);
    }
}
