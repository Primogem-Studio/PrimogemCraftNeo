package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;

public class CrystalOreBlock extends Block {
    private static final float SHATTER_VOLUME = 0.5F;
    private static final float SHATTER_PITCH = 1.0F;

    public CrystalOreBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        shatter(world, pos);
    }

    public static void shatter(World world, BlockPos pos) {
        if (world.isClient()) return;
        world.playSound(null, pos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, SHATTER_VOLUME, SHATTER_PITCH);
    }
}
