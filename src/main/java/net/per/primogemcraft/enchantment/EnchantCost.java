package net.per.primogemcraft.enchantment;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.util.PlayerItems;

import java.util.function.Consumer;
import java.util.function.Predicate;

public record EnchantCost(Component description, Predicate<ServerPlayer> payable, Consumer<ServerPlayer> pay) {
    private static final String FREE_KEY = "gui.primogemcraft.enchant_choice.cost.free";
    private static final String LEVELS_KEY = "gui.primogemcraft.enchant_choice.cost.levels";
    private static final String ITEMS_KEY = "gui.primogemcraft.enchant_choice.cost.items";
    private static final EnchantCost FREE = new EnchantCost(Component.translatable(FREE_KEY), player -> true, player -> {
    });

    public static EnchantCost free() {
        return FREE;
    }

    public static EnchantCost levels(int levels) {
        var cost = Math.max(0, levels);
        return new EnchantCost(Component.translatable(LEVELS_KEY, cost), player -> player.experienceLevel >= cost, player -> player.giveExperienceLevels(-cost));
    }

    public static EnchantCost items(ItemStack price) {
        var stack = price.copy();
        var amount = stack.getCount();
        return new EnchantCost(Component.translatable(ITEMS_KEY, amount, stack.getHoverName()), player -> PlayerItems.count(player, stack.getItem()) >= amount, player -> PlayerItems.take(player, stack.getItem(), amount));
    }

    public static EnchantCost of(Component description, Predicate<ServerPlayer> payable, Consumer<ServerPlayer> pay) {
        return new EnchantCost(description, payable, pay);
    }
}
