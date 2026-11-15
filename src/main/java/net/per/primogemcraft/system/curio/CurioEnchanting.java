package net.per.primogemcraft.system.curio;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 奇物附魔。
 *
 * <p>1.21 的附魔是数据驱动的 {@code Holder<Enchantment>}，并用
 * {@code EnchantmentTags.IN_ENCHANTING_TABLE} 标签筛选可上表附魔；
 * 1.20.1 的附魔是注册表对象（裸 {@code Enchantment}），没有该标签，
 * 改用 {@code isDiscoverable() && !isTreasureOnly()} 等价判定，
 * 附魔读写用 {@code Map<Enchantment, Integer>}。
 */
public final class CurioEnchanting {
    private CurioEnchanting() {
    }

    public static void tablePool(ServerPlayerEntity player, ItemStack target, int level) {
        var result = tablePoolResult(player, target, level);
        if (!result.isEmpty()) applyResult(target, result);
    }

    public static ItemStack tablePoolResult(ServerPlayerEntity player, ItemStack target, int level) {
        var result = target.copy();
        var available = EnchantmentHelper.getPossibleEntries(level, result, false);
        var pool = new ArrayList<EnchantmentLevelEntry>();
        for (var instance : available) {
            var enchantment = instance.enchantment;
            if (!enchantment.isTreasure()) pool.add(instance);
        }
        if (pool.isEmpty()) return ItemStack.EMPTY;
        var chosen = pool.get(player.getRandom().nextInt(pool.size()));
        return withEnchantment(result, chosen);
    }

    public static ItemStack everyPoolResult(ServerPlayerEntity player, ItemStack target, int level) {
        var result = target.copy();
        var available = EnchantmentHelper.getPossibleEntries(level, result, true);
        if (available.isEmpty()) return ItemStack.EMPTY;
        var chosen = available.get(player.getRandom().nextInt(available.size()));
        return withEnchantment(result, chosen);
    }

    public static EnchantmentLevelEntry randomEnchantment(ServerPlayerEntity player) {
        var all = new ArrayList<>(player.getServerWorld().getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT).stream().toList());
        var enchantment = all.get(player.getRandom().nextInt(all.size()));
        return new EnchantmentLevelEntry(enchantment,
                MathHelper.nextInt(player.getRandom(), 1, Math.max(1, enchantment.getMaxLevel())));
    }

    public static Optional<EnchantmentLevelEntry> inventoryEnchantment(ServerPlayerEntity player, ItemStack excluded) {
        var pool = new ArrayList<EnchantmentLevelEntry>();
        for (var stack : Curios.inventory(player)) {
            if (stack == excluded) continue;
            for (var entry : EnchantmentHelper.get(stack).entrySet())
                pool.add(new EnchantmentLevelEntry(entry.getKey(), entry.getValue()));
        }
        return pool.isEmpty() ? Optional.empty() : Optional.of(pool.get(player.getRandom().nextInt(pool.size())));
    }

    public static ItemStack withEnchantment(ItemStack target, EnchantmentLevelEntry enchantment) {
        var result = target.copy();
        var enchantments = new HashMap<>(EnchantmentHelper.get(result));
        var current = enchantments.getOrDefault(enchantment.enchantment, 0);
        enchantments.put(enchantment.enchantment, Math.max(current, enchantment.level));
        EnchantmentHelper.set(enchantments, result);
        return result;
    }

    public static void applyResult(ItemStack target, ItemStack result) {
        EnchantmentHelper.set(EnchantmentHelper.get(result), target);
    }

    public static void mergeResult(ItemStack target, ItemStack result) {
        var merged = new HashMap<>(EnchantmentHelper.get(target));
        for (var entry : EnchantmentHelper.get(result).entrySet())
            merged.merge(entry.getKey(), entry.getValue(), Math::max);
        EnchantmentHelper.set(merged, target);
    }

    public static void raise(ItemStack target, int amount) {
        if (amount <= 0 || !target.hasEnchantments()) return;
        var enchantments = new HashMap<Enchantment, Integer>();
        for (var entry : EnchantmentHelper.get(target).entrySet()) {
            var max = entry.getKey().getMaxLevel();
            enchantments.put(entry.getKey(), Math.min(entry.getValue() + amount, max));
        }
        EnchantmentHelper.set(enchantments, target);
    }
}
