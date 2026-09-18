package net.per.primogemcraft.system.wish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.*;

public class WishDrops extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "loot_table/wish";

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final List<String> CONTAINER_KEYS = List.of("pools", "entries", "children");

    private static Map<ResourceLocation, List<Item>> drops = Map.of();

    public WishDrops() {
        super(GSON, DIRECTORY);
    }

    public static List<Item> of(WishBanner banner, WishRarity rarity) {
        return drops.getOrDefault(rarity.lootTable(banner, false), List.of());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        var collected = new LinkedHashMap<ResourceLocation, Set<Item>>();
        for (var resource : resources.entrySet()) {
            if (!resource.getValue().isJsonObject()) continue;
            var tableId = ResourceLocation.fromNamespaceAndPath(resource.getKey().getNamespace(), "wish/" + resource.getKey().getPath());
            collectItems(resource.getValue().getAsJsonObject(), collected.computeIfAbsent(tableId, id -> new LinkedHashSet<>()));
        }
        var result = new LinkedHashMap<ResourceLocation, List<Item>>();
        for (var entry : collected.entrySet()) result.put(entry.getKey(), List.copyOf(entry.getValue()));
        drops = Map.copyOf(result);
    }

    private static void collectItems(JsonElement element, Set<Item> output) {
        if (element == null) return;
        if (element.isJsonArray()) {
            for (var child : element.getAsJsonArray()) collectItems(child, output);
            return;
        }
        if (!element.isJsonObject()) return;
        var object = element.getAsJsonObject();
        var name = object.get("name");
        if (name != null && name.isJsonPrimitive()) {
            BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(name.getAsString())).ifPresent(output::add);
        }
        for (var key : CONTAINER_KEYS) collectItems(object.get(key), output);
    }
}
