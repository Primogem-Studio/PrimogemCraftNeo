package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.util.TriState;

public class CrystalOreBlock extends Block {
    public static final MapCodec<CrystalOreBlock> CODEC = simpleCodec(CrystalOreBlock::new);

    private static final float SHATTER_VOLUME = 0.5F;
    private static final float SHATTER_PITCH = 1.0F;

    public CrystalOreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plant) {
        return TriState.TRUE;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        var removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        shatter(level, pos);
        return removed;
    }

    public static void shatter(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, SHATTER_VOLUME, SHATTER_PITCH);
    }
}
