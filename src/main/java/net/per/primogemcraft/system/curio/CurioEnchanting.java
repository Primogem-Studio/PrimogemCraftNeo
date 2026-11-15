package net.per.primogemcraft.system.curio;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

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

    public static void tablePool(ServerPlayer player, ItemStack target, int level) {
        var result = tablePoolResult(player, target, level);
        if (!result.isEmpty()) applyResult(target, result);
    }

    public static ItemStack tablePoolResult(ServerPlayer player, ItemStack target, int level) {
        var result = target.copy();
        var available = EnchantmentHelper.getAvailableEnchantmentResults(level, result, false);
        var pool = new ArrayList<EnchantmentInstance>();
        for (var instance : available) {
            var enchantment = instance.enchantment;
            // 等价于 1.21 的 IN_ENCHANTING_TABLE 标签：可被发现且非宝藏附魔
            if (enchantment.isDiscoverable() && !enchantment.isTreasureOnly()) pool.add(instance);
        }
        if (pool.isEmpty()) return ItemStack.EMPTY;
        var chosen = pool.get(player.getRandom().nextInt(pool.size()));
        return withEnchantment(result, chosen);
    }

    public static ItemStack everyPoolResult(ServerPlayer player, ItemStack target, int level) {
        var result = target.copy();
        var available = EnchantmentHelper.getAvailableEnchantmentResults(level, result, true);
        if (available.isEmpty()) return ItemStack.EMPTY;
        var chosen = available.get(player.getRandom().nextInt(available.size()));
        return withEnchantment(result, chosen);
    }

    public static EnchantmentInstance randomEnchantment(ServerPlayer player) {
        var all = new ArrayList<>(player.serverLevel().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT).stream().toList());
        var enchantment = all.get(player.getRandom().nextInt(all.size()));
        return new EnchantmentInstance(enchantment,
                Mth.nextInt(player.getRandom(), 1, Math.max(1, enchantment.getMaxLevel())));
    }

    public static Optional<EnchantmentInstance> inventoryEnchantment(ServerPlayer player, ItemStack excluded) {
        var pool = new ArrayList<EnchantmentInstance>();
        for (var stack : Curios.inventory(player)) {
            if (stack == excluded) continue;
            for (var entry : EnchantmentHelper.getEnchantments(stack).entrySet())
                pool.add(new EnchantmentInstance(entry.getKey(), entry.getValue()));
        }
        return pool.isEmpty() ? Optional.empty() : Optional.of(pool.get(player.getRandom().nextInt(pool.size())));
    }

    public static ItemStack withEnchantment(ItemStack target, EnchantmentInstance enchantment) {
        var result = target.copy();
        var enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(result));
        var current = enchantments.getOrDefault(enchantment.enchantment, 0);
        enchantments.put(enchantment.enchantment, Math.max(current, enchantment.level));
        EnchantmentHelper.setEnchantments(enchantments, result);
        return result;
    }

    public static void applyResult(ItemStack target, ItemStack result) {
        EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(result), target);
    }

    public static void mergeResult(ItemStack target, ItemStack result) {
        var merged = new HashMap<>(EnchantmentHelper.getEnchantments(target));
        for (var entry : EnchantmentHelper.getEnchantments(result).entrySet())
            merged.merge(entry.getKey(), entry.getValue(), Math::max);
        EnchantmentHelper.setEnchantments(merged, target);
    }

    public static void raise(ItemStack target, int amount) {
        if (amount <= 0 || !target.isEnchanted()) return;
        var enchantments = new HashMap<Enchantment, Integer>();
        for (var entry : EnchantmentHelper.getEnchantments(target).entrySet()) {
            var max = entry.getKey().getMaxLevel();
            enchantments.put(entry.getKey(), Math.min(entry.getValue() + amount, max));
        }
        EnchantmentHelper.setEnchantments(enchantments, target);
    }
}
