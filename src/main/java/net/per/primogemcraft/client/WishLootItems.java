package net.per.primogemcraft.client;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.item.Item;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.per.primogemcraft.system.wish.WishRarity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WishLootItems {
    private static volatile Map<Identifier, List<Item>> drops = Map.of();
    private static Runnable updateListener = () -> {};

    public static void setUpdateListener(Runnable listener) {
        updateListener = listener;
    }

    public static List<Item> of(WishRarity rarity) {
        return drops.getOrDefault(rarity.lootTable(), List.of());
    }

    public static void update(Map<Identifier, List<Identifier>> tables) {
        var result = new LinkedHashMap<Identifier, List<Item>>();
        tables.forEach((table, items) -> result.put(table, items.stream()
                .flatMap(id -> Registries.ITEM.getOrEmpty(id).stream()).toList()));
        drops = Map.copyOf(result);
        updateListener.run();
    }

    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            drops = Map.of();
            updateListener.run();
        });
    }
}
