package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;

import java.util.function.Function;

public class PGCHorizontalBlock extends HorizontalFacingBlock {
    private final Function<BlockState, VoxelShape> shape;

    public PGCHorizontalBlock(AbstractBlock.Settings settings, VoxelShape shape) {
        this(settings, state -> shape);
    }

    public PGCHorizontalBlock(AbstractBlock.Settings settings, Function<BlockState, VoxelShape> shape) {
        super(settings);
        this.shape = shape;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var offset = state.getModelOffset(world, pos);
        return shape.apply(state).offset(offset.x, offset.y, offset.z);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
