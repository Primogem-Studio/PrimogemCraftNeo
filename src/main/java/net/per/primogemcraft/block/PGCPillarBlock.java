package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;

import java.util.function.Function;

public class PGCPillarBlock extends PillarBlock {
    private final Function<BlockState, VoxelShape> shape;

    public PGCPillarBlock(AbstractBlock.Settings properties, Function<BlockState, VoxelShape> shape) {
        super(properties);
        this.shape = shape;
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        var offset = state.getModelOffset(level, pos);
        return shape.apply(state).offset(offset.x, offset.y, offset.z);
    }
}
