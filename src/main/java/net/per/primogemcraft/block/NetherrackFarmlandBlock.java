package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;

public class NetherrackFarmlandBlock extends PGCShapeBlock {
    public static final MapCodec<NetherrackFarmlandBlock> CODEC =
            simpleCodec(properties -> new NetherrackFarmlandBlock(properties, Shapes.block()));

    public NetherrackFarmlandBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plant) {
        return TriState.TRUE;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 1;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        if (level.isClientSide()) return;
        var above = level.getBlockState(pos.above());
        if (above.isAir() || above.is(BlockTags.CROPS) || above.is(Blocks.WATER) || above.is(Blocks.LAVA)) return;
        level.setBlock(pos, Blocks.NETHERRACK.defaultBlockState(), Block.UPDATE_ALL);
    }
}
