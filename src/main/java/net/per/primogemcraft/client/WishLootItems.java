package net.per.primogemcraft.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.per.primogemcraft.system.wish.WishRarity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WishLootItems {
    private static final Component VARIABLE = Component.translatable("rei.primogemcraft.wish.chance_variable");
    private static final MathContext PERCENT_PRECISION = new MathContext(4);
    private static volatile Map<ResourceLocation, Map<Item, Component>> drops = Map.of();
    private static Runnable updateListener = () -> {};

    public static void setUpdateListener(Runnable listener) {
        updateListener = listener;
    }

    public static Map<Item, Component> of(WishRarity rarity) {
        return drops.getOrDefault(rarity.lootTable(), Map.of());
    }

    public static Component chance(WishRarity rarity, Item item) {
        return of(rarity).getOrDefault(item, VARIABLE);
    }

    public static void update(Map<ResourceLocation, Map<ResourceLocation, Double>> tables) {
        var result = new LinkedHashMap<ResourceLocation, Map<Item, Component>>();
        tables.forEach((table, items) -> {
            var labels = new LinkedHashMap<Item, Component>();
            items.forEach((id, chance) -> BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> labels.put(item, label(chance))));
            result.put(table, Collections.unmodifiableMap(labels));
        });
        drops = Map.copyOf(result);
        updateListener.run();
    }

    private static Component label(double chance) {
        if (!Double.isFinite(chance)) return VARIABLE;
        var percent = BigDecimal.valueOf(chance).movePointRight(2).round(PERCENT_PRECISION).stripTrailingZeros();
        return Component.translatable("rei.primogemcraft.wish.chance", percent.scale() > 8 ? percent.toString() : percent.toPlainString());
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        drops = Map.of();
        updateListener.run();
    }
}
