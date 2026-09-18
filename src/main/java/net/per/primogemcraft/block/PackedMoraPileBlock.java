package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.per.primogemcraft.entity.misc.FallingMoraPileEntity;

public class PackedMoraPileBlock extends FallingBlock {
    public static final MapCodec<PackedMoraPileBlock> CODEC = simpleCodec(PackedMoraPileBlock::new);

    public PackedMoraPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            FallingMoraPileEntity.fall(level, pos, state);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        crushGlassBelow(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, movedByPiston);
        crushGlassBelow(level, pos);
    }

    private static void crushGlassBelow(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        var below = pos.below();
        var state = level.getBlockState(below);
        if (!state.is(Tags.Blocks.GLASS_BLOCKS)) return;
        level.levelEvent(2001, below, Block.getId(state));
        level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
