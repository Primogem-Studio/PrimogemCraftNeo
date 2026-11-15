package net.per.primogemcraft.compat.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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

    public static boolean any(Inventory inventory, Predicate<ItemStack> predicate) {
        if (inventory == null) return false;
        return any(inventory.items, predicate) || any(inventory.armor, predicate) || any(inventory.offhand, predicate);
    }

    private static boolean any(NonNullList<ItemStack> stacks, Predicate<ItemStack> predicate) {
        for (var stack : stacks) if (predicate.test(stack)) return true;
        return false;
    }
}
