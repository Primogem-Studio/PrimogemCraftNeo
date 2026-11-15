package net.per.primogemcraft.system.wish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.item.Item;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WishDrops extends JsonDataLoader implements net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {
    public static final String DIRECTORY = "loot_table/wish";

    @Override
    public Identifier getFabricId() {
        return Identifier.of("primogemcraft", "wish_drops");
    }

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final List<String> CONTAINER_KEYS = List.of("pools", "entries", "children");

    private static Map<Identifier, List<Item>> drops = Map.of();

    public WishDrops() {
        super(GSON, DIRECTORY);
    }

    public static List<Item> of(WishRarity rarity) {
        return drops.getOrDefault(rarity.lootTable(), List.of());
    }

    public static Map<Identifier, List<Identifier>> displayItems() {
        var result = new LinkedHashMap<Identifier, List<Identifier>>();
        for (var rarity : WishRarity.values()) {
            result.put(rarity.lootTable(), of(rarity).stream().map(Registries.ITEM::getId).toList());
        }
        return Map.copyOf(result);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager, Profiler profiler) {
        var collected = new LinkedHashMap<Identifier, Set<Item>>();
        for (var resource : resources.entrySet()) {
            if (!resource.getValue().isJsonObject()) continue;
            var tableId = Identifier.of(resource.getKey().getNamespace(), "wish/" + resource.getKey().getPath());
            collectItems(resource.getValue().getAsJsonObject(), collected.computeIfAbsent(tableId, id -> new LinkedHashSet<>()));
        }
        var result = new LinkedHashMap<Identifier, List<Item>>();
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
            Registries.ITEM.getOrEmpty(Identifier.tryParse(name.getAsString())).ifPresent(output::add);
        }
        for (var key : CONTAINER_KEYS) collectItems(object.get(key), output);
    }
}
