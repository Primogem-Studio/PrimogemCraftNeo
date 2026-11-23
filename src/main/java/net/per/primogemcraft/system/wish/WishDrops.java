package net.per.primogemcraft.system.wish;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.per.primogemcraft.PrimogemCraft;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class WishDrops extends SimpleJsonResourceReloadListener {
    private static final FileToIdConverter LOOT_FILES = FileToIdConverter.json("loot_table");
    private static final Map<ReloadableServerRegistries.Holder, Map<ResourceLocation, LootTable>> WISH_TABLES =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final ReloadableServerRegistries.Holder registries;

    public WishDrops(ReloadableServerRegistries.Holder registries) {
        super(new Gson(), "loot_table/wish");
        this.registries = registries;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        var tables = new HashMap<ResourceLocation, LootTable>();
        for (var rarity : WishRarity.values()) {
            var id = rarity.lootTable();
            var source = resources.get(id.withPath(rarity.id()));
            var definition = expand(source, manager, new HashSet<>(Set.of(id)));
            var table = LootTable.DIRECT_CODEC.parse(makeConditionalOps(), definition).getOrThrow();
            tables.put(id, table);
        }
        WISH_TABLES.put(registries, Map.copyOf(tables));
    }

    private JsonObject expand(JsonElement source, ResourceManager manager, Set<ResourceLocation> path) {
        if (source == null) return new JsonObject();
        var table = ConditionalOps.createConditionalCodec(ExtraCodecs.JSON).parse(makeConditionalOps(), source)
                .getOrThrow().map(JsonElement::getAsJsonObject).orElseGet(JsonObject::new);
        inlineReferences(table, manager, path);
        return table;
    }

    private void inlineReferences(JsonObject table, ResourceManager manager, Set<ResourceLocation> path) {
        if (!table.has("pools")) return;
        for (var pool : table.getAsJsonArray("pools")) {
            var entries = pool.getAsJsonObject().getAsJsonArray("entries");
            if (entries != null) entries.forEach(entry -> inlineEntry(entry.getAsJsonObject(), manager, path));
        }
    }

    private void inlineEntry(JsonObject entry, ResourceManager manager, Set<ResourceLocation> path) {
        if (!entry.has("type")) return;
        var type = ResourceLocation.parse(entry.get("type").getAsString());
        if (!type.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) return;
        switch (type.getPath()) {
            case "group", "alternatives", "sequence" -> {
                entry.getAsJsonArray("children").forEach(child -> inlineEntry(child.getAsJsonObject(), manager, path));
                return;
            }
            case "loot_table" -> {}
            default -> { return; }
        }
        var value = entry.get("value");
        if (value.isJsonObject()) {
            inlineReferences(value.getAsJsonObject(), manager, path);
            return;
        }
        var id = ResourceLocation.parse(value.getAsString());
        var file = manager.getResource(LOOT_FILES.idToFile(id));
        if (file.isEmpty() || !path.add(id)) {
            entry.add("value", new JsonObject());
            return;
        }
        try (var reader = file.get().openAsReader()) {
            entry.add("value", expand(JsonParser.parseReader(reader), manager, path));
        } catch (IOException error) {
            throw new UncheckedIOException("Cannot read wish loot table " + id, error);
        } finally {
            path.remove(id);
        }
    }

    @SuppressWarnings("deprecation")
    public static List<ItemStack> roll(WishRarity rarity, LootParams params) {
        var level = params.getLevel();
        var tables = WISH_TABLES.getOrDefault(level.getServer().reloadableRegistries(), Map.of());
        var table = tables.getOrDefault(rarity.lootTable(), LootTable.EMPTY);
        var stacks = new ArrayList<ItemStack>();
        table.getRandomItemsRaw(params, LootTable.createStackSplitter(level, stacks::add));
        return stacks;
    }

    public static Map<ResourceLocation, Map<ResourceLocation, Double>> displayItems(ReloadableServerRegistries.Holder registries) {
        var ops = registries.get().createSerializationContext(JsonOps.INSTANCE);
        var wishTables = WISH_TABLES.get(registries);
        if (wishTables == null) return Map.of();
        var tables = new HashMap<String, JsonObject>();
        wishTables.forEach((id, table) -> tables.put(id.toString(), LootTable.DIRECT_CODEC.encodeStart(ops, table)
                .resultOrPartial(error -> PrimogemCraft.LOGGER.warn("Cannot read wish loot table {}: {}", id, error))
                .map(JsonElement::getAsJsonObject).orElseGet(JsonObject::new)));
        var chances = new WishLootChances(tables::get, name -> BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, ResourceLocation.parse(name)))
                .stream().flatMap(tag -> tag.stream()).map(item -> BuiltInRegistries.ITEM.getKey(item.value()).toString()).toList());
        var result = new LinkedHashMap<ResourceLocation, Map<ResourceLocation, Double>>();
        for (var rarity : WishRarity.values()) {
            var items = new LinkedHashMap<ResourceLocation, Double>();
            chances.forTable(rarity.lootTable().toString()).forEach((name, chance) ->
                    items.put(ResourceLocation.parse(name), chance));
            result.put(rarity.lootTable(), items);
        }
        return result;
    }
}
