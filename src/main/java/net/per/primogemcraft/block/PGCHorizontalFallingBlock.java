package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.world.BlockView;
import net.minecraft.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;

import java.util.function.Function;

public class PGCHorizontalFallingBlock extends FallingBlock {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private final Function<BlockState, VoxelShape> shape;

    public PGCHorizontalFallingBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        this(properties, state -> shape);
    }

    public PGCHorizontalFallingBlock(AbstractBlock.Settings properties, Function<BlockState, VoxelShape> shape) {
        super(properties);
        this.shape = shape;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        var offset = state.getModelOffset(level, pos);
        return shape.apply(state).offset(offset.x, offset.y, offset.z);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.util.BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, net.minecraft.util.BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
