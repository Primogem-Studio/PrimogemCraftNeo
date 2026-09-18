package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class PGCPillarBlock extends RotatedPillarBlock {
    public static final MapCodec<PGCPillarBlock> CODEC = simpleCodec(properties -> new PGCPillarBlock(properties, state -> Shapes.block()));

    private final Function<BlockState, VoxelShape> shape;

    public PGCPillarBlock(BlockBehaviour.Properties properties, Function<BlockState, VoxelShape> shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos);
        return shape.apply(state).move(offset.x, offset.y, offset.z);
    }
}
