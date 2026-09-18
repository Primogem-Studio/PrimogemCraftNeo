package net.per.primogemcraft.enchantment;

import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record EnchantOption(ItemStack target, Supplier<ItemStack> source, EnchantGrade grade, int level, EnchantCost cost) {
    public static EnchantOption of(ItemStack target, ItemStack preview, EnchantGrade grade, int level, EnchantCost cost) {
        return new EnchantOption(target, preview::copy, grade, level, cost);
    }

    public ItemStack preview() {
        var stack = source.get();
        return stack == null ? ItemStack.EMPTY : stack;
    }
}
