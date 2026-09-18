package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class PGCFaceAttachedBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<AttachFace> FACE = FaceAttachedHorizontalDirectionalBlock.FACE;
    public static final MapCodec<PGCFaceAttachedBlock> CODEC =
            simpleCodec(properties -> new PGCFaceAttachedBlock(properties, state -> Shapes.block()));

    private final Function<BlockState, VoxelShape> shape;

    public PGCFaceAttachedBlock(BlockBehaviour.Properties properties, Function<BlockState, VoxelShape> shape) {
        super(properties);
        this.shape = shape;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var clicked = context.getClickedFace();
        if (clicked.getAxis() == Direction.Axis.Y) {
            return defaultBlockState()
                    .setValue(FACE, clicked == Direction.DOWN ? AttachFace.CEILING : AttachFace.FLOOR)
                    .setValue(FACING, context.getHorizontalDirection());
        }
        return defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, clicked);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos);
        return shape.apply(state).move(offset.x, offset.y, offset.z);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }
}
