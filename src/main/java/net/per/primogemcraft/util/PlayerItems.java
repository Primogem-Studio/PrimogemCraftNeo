package net.per.primogemcraft.util;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PlayerItems {
    private PlayerItems() {
    }

    public static int count(ServerPlayer player, Item item) {
        return count(player.getInventory().items, item) + count(player.getInventory().offhand, item);
    }

    public static boolean take(ServerPlayer player, Item item, int amount) {
        var taken = take(player.getInventory().items, item, amount);
        taken += take(player.getInventory().offhand, item, amount - taken);
        return taken > 0;
    }

    public static void give(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        var giving = stack.copy();
        player.getInventory().add(giving);
        if (giving.isEmpty()) return;
        var dropped = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), giving);
        dropped.setDefaultPickUpDelay();
        player.level().addFreshEntity(dropped);
    }

    private static int count(NonNullList<ItemStack> stacks, Item item) {
        var total = 0;
        for (var stack : stacks) if (stack.is(item)) total += stack.getCount();
        return total;
    }

    private static int take(NonNullList<ItemStack> stacks, Item item, int amount) {
        var taken = 0;
        for (var stack : stacks) {
            if (taken >= amount) break;
            if (!stack.is(item)) continue;
            var takenHere = Math.min(amount - taken, stack.getCount());
            stack.shrink(takenHere);
            taken += takenHere;
        }
        return taken;
    }
}
