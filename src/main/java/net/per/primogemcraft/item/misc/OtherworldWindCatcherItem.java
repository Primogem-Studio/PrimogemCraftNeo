package net.per.primogemcraft.item.misc;

import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;

public class OtherworldWindCatcherItem extends DescribedItem {
    private final boolean filled;

    public OtherworldWindCatcherItem(Settings properties, boolean filled) {
        super(properties);
        this.filled = filled;
    }

    public boolean filled() {
        return filled;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(stack);
        if (filled == player.isSneaking()) return TypedActionResult.pass(stack);
        if (level.isClient()) return TypedActionResult.success(stack, true);
        WindCatchers.use(level, player, filled);
        return TypedActionResult.success(stack, false);
    }
}
