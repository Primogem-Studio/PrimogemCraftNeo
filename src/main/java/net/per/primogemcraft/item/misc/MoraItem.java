package net.per.primogemcraft.item.misc;

import net.minecraft.util.math.Direction;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ActionResult;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.block.Block;
import net.per.primogemcraft.block.MoraPileBlock;
import net.per.primogemcraft.registry.PGCBlocks;

public class MoraItem extends Item {
    private static final float PLACE_VOLUME = 0.5F;
    private static final float PLACE_PITCH_MIN = 1.0F;
    private static final float PLACE_PITCH_MAX = 1.1F;

    public MoraItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var level = context.getWorld();
        var player = context.getPlayer();
        var clicked = context.getBlockPos();
        if (context.getStack().isEmpty()) return ActionResult.PASS;
        if (level.getBlockState(clicked).getBlock() instanceof MoraPileBlock) return ActionResult.PASS;
        var target = clicked.offset(context.getSide());
        if (!level.getBlockState(target).isReplaceable()) return ActionResult.PASS;
        if (level.isClient()) return ActionResult.SUCCESS;
        var facing = player == null ? Direction.NORTH : player.getMovementDirection().getOpposite();
        if (!level.setBlockState(target, PGCBlocks.MORA_PILE.get().getDefaultState().with(MoraPileBlock.FACING, facing), Block.NOTIFY_ALL)) {
            return ActionResult.FAIL;
        }
        level.playSound(null, target, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, PLACE_VOLUME,
                MathHelper.nextFloat(level.getRandom(), PLACE_PITCH_MIN, PLACE_PITCH_MAX));
        context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
