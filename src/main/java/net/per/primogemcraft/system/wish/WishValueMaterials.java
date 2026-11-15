package net.per.primogemcraft.system.wish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class WishValueMaterials extends JsonDataLoader implements net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener {
    public static final String DIRECTORY = "wish_value_materials";

    @Override
    public Identifier getFabricId() {
        return Identifier.of("primogemcraft", "wish_value_materials");
    }
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static Map<Item, Integer> itemValues = Map.of();
    private static Map<TagKey<Item>, Integer> tagValues = Map.of();

    public WishValueMaterials() {
        super(GSON, DIRECTORY);
    }

    public static int value(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var direct = itemValues.get(stack.getItem());
        if (direct != null) return direct;
        for (var entry : tagValues.entrySet()) if (stack.isIn(entry.getKey())) return entry.getValue();
        return 1;
    }

    public static Map<Identifier, Integer> displayValues() {
        var values = new LinkedHashMap<Identifier, Integer>();
        for (var item : Registries.ITEM) {
            var value = value(item.getDefaultStack());
            if (value > 1) values.put(Registries.ITEM.getId(item), value);
        }
        return values;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager, Profiler profiler) {
        var items = new LinkedHashMap<Item, Integer>();
        var tags = new LinkedHashMap<TagKey<Item>, Integer>();
        for (var resource : resources.values()) {
            var values = resource.isJsonObject() ? resource.getAsJsonObject().get("values") : null;
            if (values == null || !values.isJsonObject()) continue;
            for (var entry : values.getAsJsonObject().entrySet()) {
                var value = entry.getValue().getAsInt();
                if (value <= 0) continue;
                var key = entry.getKey();
                if (key.startsWith("#")) {
                    tags.put(TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(key.substring(1))), value);
                } else {
                    Registries.ITEM.getOrEmpty(Identifier.tryParse(key)).ifPresent(item -> items.put(item, value));
                }
            }
        }
        itemValues = Map.copyOf(items);
        tagValues = Map.copyOf(tags);
    }
}
