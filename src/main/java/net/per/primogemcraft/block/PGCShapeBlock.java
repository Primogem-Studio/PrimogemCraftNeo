package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PGCShapeBlock extends Block {
    public static final MapCodec<PGCShapeBlock> CODEC = simpleCodec(properties -> new PGCShapeBlock(properties, Shapes.block()));

    private final VoxelShape shape;

    public PGCShapeBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var offset = state.getOffset(level, pos);
        return shape.move(offset.x, offset.y, offset.z);
    }
}
