package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;

public class PGCShapeBlock extends Block {
    private final VoxelShape shape;

    public PGCShapeBlock(AbstractBlock.Settings properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        var offset = state.getModelOffset(level, pos);
        return shape.offset(offset.x, offset.y, offset.z);
    }
}
