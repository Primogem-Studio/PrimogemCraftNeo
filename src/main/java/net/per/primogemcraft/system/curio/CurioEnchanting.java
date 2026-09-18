package net.per.primogemcraft.system.curio;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class CurioEnchanting {
    private CurioEnchanting() {
    }

    public static void tablePool(ServerPlayer player, ItemStack target, int level) {
        var result = tablePoolResult(player, target, level);
        if (!result.isEmpty()) applyResult(target, result);
    }

    public static ItemStack tablePoolResult(ServerPlayer player, ItemStack target, int level) {
        var candidates = registry(player).getTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        return candidates.map(holders -> roll(player, target, level, holders.stream())).orElse(ItemStack.EMPTY);
    }

    public static ItemStack everyPoolResult(ServerPlayer player, ItemStack target, int level) {
        return roll(player, target, level, registry(player).holders());
    }

    public static EnchantmentInstance randomEnchantment(ServerPlayer player) {
        var holders = registry(player).holders().toList();
        var holder = holders.get(player.getRandom().nextInt(holders.size()));
        return new EnchantmentInstance(holder, Mth.nextInt(player.getRandom(), 1, Math.max(1, holder.value().getMaxLevel())));
    }

    public static Optional<EnchantmentInstance> inventoryEnchantment(ServerPlayer player, ItemStack excluded) {
        var pool = new ArrayList<EnchantmentInstance>();
        for (var stack : Curios.inventory(player)) {
            if (stack == excluded) continue;
            for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet())
                pool.add(new EnchantmentInstance(entry.getKey(), entry.getIntValue()));
        }
        return pool.isEmpty() ? Optional.empty() : Optional.of(pool.get(player.getRandom().nextInt(pool.size())));
    }

    public static ItemStack withEnchantment(ItemStack target, EnchantmentInstance enchantment) {
        var result = target.copy();
        EnchantmentHelper.updateEnchantments(result, mutable -> mutable.upgrade(enchantment.enchantment, enchantment.level));
        return result;
    }

    public static void applyResult(ItemStack target, ItemStack result) {
        EnchantmentHelper.setEnchantments(target, EnchantmentHelper.getEnchantmentsForCrafting(result));
    }

    public static void mergeResult(ItemStack target, ItemStack result) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(result);
        EnchantmentHelper.updateEnchantments(target, mutable -> {
            for (var entry : enchantments.entrySet()) mutable.upgrade(entry.getKey(), entry.getIntValue());
        });
    }

    public static void raise(ItemStack target, int amount) {
        if (amount <= 0 || !target.isEnchanted()) return;
        EnchantmentHelper.updateEnchantments(target, mutable -> {
            for (var holder : List.copyOf(mutable.keySet())) mutable.set(holder, Math.min(mutable.getLevel(holder) + amount, holder.value().getMaxLevel()));
        });
    }

    @SuppressWarnings("unchecked")
    private static ItemStack roll(ServerPlayer player, ItemStack target, int level, Stream<? extends Holder<Enchantment>> candidates) {
        return EnchantmentHelper.enchantItem(player.getRandom(), target.copy(), level, (Stream<Holder<Enchantment>>) candidates);
    }

    private static Registry<Enchantment> registry(ServerPlayer player) {
        return player.serverLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
    }
}
