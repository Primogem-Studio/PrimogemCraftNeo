package net.per.primogemcraft.compat.item;

import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Enchantment access for 1.20.1.
 *
 * <p>1.21 moved enchantments to registry holders and exposed
 * {@code stack.getEnchantmentLevel(Holder)}, {@code stack.supportsEnchantment(Holder)}
 * and {@code EnchantmentHelper.updateEnchantments(stack, consumer)}.
 * 1.20.1 uses plain {@link Enchantment} objects plus a
 * {@code Map<Enchantment, Integer>} read/write pair, so those call sites are
 * routed through here.
 */
public final class Enchantments120 {

    private Enchantments120() {
    }

    public static int level(ItemStack stack, Enchantment enchantment) {
        if (stack == null || stack.isEmpty() || enchantment == null) return 0;
        return EnchantmentHelper.getLevel(enchantment, stack);
    }

    /** 1.21 {@code stack.supportsEnchantment(holder)} equivalent. */
    public static boolean supports(ItemStack stack, Enchantment enchantment) {
        if (stack == null || stack.isEmpty() || enchantment == null) return false;
        return enchantment.isAcceptableItem(stack);
    }

    /**
     * 1.21 {@code EnchantmentHelper.updateEnchantments(stack, mutable -> ...)} equivalent.
     *
     * <p>The consumer receives a {@link Mutable} view; levels set to 0 or below remove
     * the entry, matching 1.21 semantics.
     */
    public static void update(ItemStack stack, Consumer<Mutable> action) {
        if (stack == null || stack.isEmpty()) return;
        var map = new LinkedHashMap<>(EnchantmentHelper.get(stack));
        var mutable = new Mutable(map);
        action.accept(mutable);
        map.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= 0);
        EnchantmentHelper.set(map, stack);
    }

    /** Minimal stand-in for 1.21's {@code ItemEnchantments.Mutable}. */
    public static final class Mutable {
        private final Map<Enchantment, Integer> map;

        private Mutable(Map<Enchantment, Integer> map) {
            this.map = map;
        }

        public void set(Enchantment enchantment, int level) {
            if (enchantment == null) return;
            map.put(enchantment, level);
        }

        public int get(Enchantment enchantment) {
            return map.getOrDefault(enchantment, 0);
        }

        public void removeIf(java.util.function.Predicate<Enchantment> predicate) {
            map.keySet().removeIf(predicate);
        }
    }
}
