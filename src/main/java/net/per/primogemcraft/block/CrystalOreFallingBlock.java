package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CrystalOreFallingBlock extends PGCFallingBlock {
    public static final MapCodec<CrystalOreFallingBlock> CODEC = simpleCodec(CrystalOreFallingBlock::new);

    public CrystalOreFallingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        var removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        CrystalOreBlock.shatter(level, pos);
        return removed;
    }
}
