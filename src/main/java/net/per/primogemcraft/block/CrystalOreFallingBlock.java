package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

public class CrystalOreFallingBlock extends PGCFallingBlock {
    public CrystalOreFallingBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        CrystalOreBlock.shatter(world, pos);
    }
}
