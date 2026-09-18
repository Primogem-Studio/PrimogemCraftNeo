package net.per.primogemcraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PGCFallingBlock extends FallingBlock {
    public static final MapCodec<PGCFallingBlock> CODEC = simpleCodec(PGCFallingBlock::new);

    public PGCFallingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }
}
