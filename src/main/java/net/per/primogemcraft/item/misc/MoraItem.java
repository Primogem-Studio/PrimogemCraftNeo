package net.per.primogemcraft.item.misc;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.per.primogemcraft.block.MoraPileBlock;
import net.per.primogemcraft.registry.PGCBlocks;

public class MoraItem extends Item {
    private static final float PLACE_VOLUME = 0.5F;
    private static final float PLACE_PITCH_MIN = 1.0F;
    private static final float PLACE_PITCH_MAX = 1.1F;

    public MoraItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var clicked = context.getClickedPos();
        if (context.getItemInHand().isEmpty()) return InteractionResult.PASS;
        if (level.getBlockState(clicked).getBlock() instanceof MoraPileBlock) return InteractionResult.PASS;
        var target = clicked.relative(context.getClickedFace());
        if (!level.getBlockState(target).canBeReplaced()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var facing = player == null ? Direction.NORTH : player.getDirection().getOpposite();
        if (!level.setBlock(target, PGCBlocks.MORA_PILE.get().defaultBlockState().setValue(MoraPileBlock.FACING, facing), Block.UPDATE_ALL)) {
            return InteractionResult.FAIL;
        }
        level.playSound(null, target, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, PLACE_VOLUME,
                Mth.nextFloat(level.getRandom(), PLACE_PITCH_MIN, PLACE_PITCH_MAX));
        context.getItemInHand().consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
