package net.per.primogemcraft.system.wish;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;

public final class WishValue {
    public static final int CAPACITY = 30;

    public static int get(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.WISH_VALUE, 0);
    }

    public static double perPull(ItemStack stack) {
        return get(stack) / (double) Math.max(1, stack.getOrDefault(PGCDataComponents.WISH_VALUE_DIVISOR, 1));
    }

    public static int absorb(ItemStack core, Player player, int limit, boolean whole) {
        var source = player.getOffhandItem();
        if (source.isEmpty() || limit <= 0) return 0;
        var perItem = WishValueMaterials.value(source);
        var consumed = whole ? Math.min(source.getCount(), limit) : 1;
        source.shrink(consumed);
        core.set(PGCDataComponents.WISH_VALUE, get(core) + consumed * perItem);
        return consumed;
    }
}
