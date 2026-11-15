package net.per.primogemcraft.system.wish;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;

public final class WishValue {
    public static final int CAPACITY = 30;

    public static int get(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.WISH_VALUE, 0);
    }

    public static int absorb(ItemStack core, PlayerEntity player, int limit, boolean whole) {
        var source = player.getOffHandStack();
        if (source.isEmpty() || limit <= 0) return 0;
        var perItem = WishValueMaterials.value(source);
        var consumed = whole ? Math.min(source.getCount(), limit) : 1;
        source.decrement(consumed);
        StackComponents.set(core, PGCDataComponents.WISH_VALUE, get(core) + consumed * perItem);
        return consumed;
    }
}
