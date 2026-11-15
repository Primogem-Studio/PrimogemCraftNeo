package net.per.primogemcraft.util;

import net.minecraft.util.collection.DefaultedList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class PlayerItems {
    private PlayerItems() {
    }

    public static int count(ServerPlayerEntity player, Item item) {
        return count(player.getInventory().main, item) + count(player.getInventory().offHand, item);
    }

    public static boolean take(ServerPlayerEntity player, Item item, int amount) {
        var taken = take(player.getInventory().main, item, amount);
        taken += take(player.getInventory().offHand, item, amount - taken);
        return taken > 0;
    }

    public static void give(ServerPlayerEntity player, ItemStack stack) {
        if (stack.isEmpty()) return;
        var giving = stack.copy();
        player.getInventory().insertStack(giving);
        if (giving.isEmpty()) return;
        var dropped = new ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), giving);
        dropped.setPickupDelay(0);
        player.getWorld().spawnEntity(dropped);
    }

    private static int count(DefaultedList<ItemStack> stacks, Item item) {
        var total = 0;
        for (var stack : stacks) if (stack.isOf(item)) total += stack.getCount();
        return total;
    }

    private static int take(DefaultedList<ItemStack> stacks, Item item, int amount) {
        var taken = 0;
        for (var stack : stacks) {
            if (taken >= amount) break;
            if (!stack.isOf(item)) continue;
            var takenHere = Math.min(amount - taken, stack.getCount());
            stack.decrement(takenHere);
            taken += takenHere;
        }
        return taken;
    }
}
