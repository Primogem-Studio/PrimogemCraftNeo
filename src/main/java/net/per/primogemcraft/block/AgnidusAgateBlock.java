package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AgnidusAgateBlock extends Block {
    public static final MapCodec<AgnidusAgateBlock> CODEC = simpleCodec(AgnidusAgateBlock::new);

    private static final float IGNITE_SECONDS = 15.0F;

    public AgnidusAgateBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.isClientSide()) return;
        entity.igniteForSeconds(IGNITE_SECONDS);
    }
}
