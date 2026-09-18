package net.per.primogemcraft.item.misc;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

public class OtherworldWindCatcherItem extends DescribedItem {
    private final boolean filled;

    public OtherworldWindCatcherItem(Properties properties, boolean filled) {
        super(properties);
        this.filled = filled;
    }

    public boolean filled() {
        return filled;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (filled == player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (level.isClientSide()) return InteractionResultHolder.sidedSuccess(stack, true);
        WindCatchers.use(level, player, filled);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
