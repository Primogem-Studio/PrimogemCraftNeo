package net.per.primogemcraft.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.per.primogemcraft.system.wish.WishRarity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WishLootItems {
    private static volatile Map<ResourceLocation, List<Item>> drops = Map.of();
    private static Runnable updateListener = () -> {};

    public static void setUpdateListener(Runnable listener) {
        updateListener = listener;
    }

    public static List<Item> of(WishRarity rarity) {
        return drops.getOrDefault(rarity.lootTable(), List.of());
    }

    public static void update(Map<ResourceLocation, List<ResourceLocation>> tables) {
        var result = new LinkedHashMap<ResourceLocation, List<Item>>();
        tables.forEach((table, items) -> result.put(table, items.stream()
                .flatMap(id -> BuiltInRegistries.ITEM.getOptional(id).stream()).toList()));
        drops = Map.copyOf(result);
        updateListener.run();
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        drops = Map.of();
        updateListener.run();
    }
}
