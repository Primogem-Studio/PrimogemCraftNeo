package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;

import java.util.function.Function;
import net.minecraft.util.Hand;

public class FaceAttachedContainerBlock extends PGCFaceAttachedBlock implements BlockEntityProvider, ContainerWindowBlock {
    private static final int CODEC_CONTAINER_SIZE = 45;

    private final int containerSize;

    public FaceAttachedContainerBlock(AbstractBlock.Settings properties, Function<BlockState, VoxelShape> shape, int containerSize) {
        super(properties, shape);
        this.containerSize = containerSize;
    }

    @Override
    public int containerSize() {
        return containerSize;
    }

    @Override
    public ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ContainerWindowBlocks.open(level, pos, player);
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.isOf(newState.getBlock())) ContainerWindowBlocks.dropContents(level, pos);
        super.onStateReplaced(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World level, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(level.getBlockEntity(pos));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ContainerWindowBlockEntity(pos, state);
    }
}
