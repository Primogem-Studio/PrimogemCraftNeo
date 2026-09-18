package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;

import java.util.function.Function;

public class FaceAttachedContainerBlock extends PGCFaceAttachedBlock implements EntityBlock, ContainerWindowBlock {
    private static final int CODEC_CONTAINER_SIZE = 45;

    public static final MapCodec<FaceAttachedContainerBlock> CODEC =
            simpleCodec(properties -> new FaceAttachedContainerBlock(properties, state -> Shapes.block(), CODEC_CONTAINER_SIZE));

    private final int containerSize;

    public FaceAttachedContainerBlock(BlockBehaviour.Properties properties, Function<BlockState, VoxelShape> shape, int containerSize) {
        super(properties, shape);
        this.containerSize = containerSize;
    }

    @Override
    public int containerSize() {
        return containerSize;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ContainerWindowBlocks.open(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) ContainerWindowBlocks.dropContents(level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ContainerWindowBlockEntity(pos, state);
    }
}
