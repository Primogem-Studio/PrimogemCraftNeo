package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;

public class AgnidusAgateBlock extends Block {
    private static final float IGNITE_SECONDS = 15.0F;

    public AgnidusAgateBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    @Override
    public void onSteppedOn(World level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClient()) return;
        entity.setOnFireFor((int) (IGNITE_SECONDS));
    }
}
