package net.per.primogemcraft.compat.item;

import net.minecraft.util.collection.DefaultedList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

/**
 * Inventory predicate helpers for 1.20.1.
 *
 * <p>1.21 added {@code Inventory.contains(Predicate<ItemStack>)}; 1.20.1 only has
 * {@code contains(ItemStack)} / {@code contains(TagKey)}. This helper scans the
 * same compartments 1.21 does (main inventory, armor, offhand).
 */
public final class Inventories120 {

    private Inventories120() {
    }

    public static boolean any(PlayerInventory inventory, Predicate<ItemStack> predicate) {
        if (inventory == null) return false;
        return any(inventory.main, predicate) || any(inventory.armor, predicate) || any(inventory.offHand, predicate);
    }

    private static boolean any(DefaultedList<ItemStack> stacks, Predicate<ItemStack> predicate) {
        for (var stack : stacks) if (predicate.test(stack)) return true;
        return false;
    }
}
