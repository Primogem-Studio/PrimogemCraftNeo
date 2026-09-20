package net.per.primogemcraft.system.wish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class WishValueMaterials extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "wish_value_materials";
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
        for (var entry : tagValues.entrySet()) if (stack.is(entry.getKey())) return entry.getValue();
        return 1;
    }

    public static Map<ResourceLocation, Integer> displayValues() {
        var values = new LinkedHashMap<ResourceLocation, Integer>();
        for (var item : BuiltInRegistries.ITEM) {
            var value = value(item.getDefaultInstance());
            if (value > 1) values.put(BuiltInRegistries.ITEM.getKey(item), value);
        }
        return values;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
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
                    tags.put(ItemTags.create(ResourceLocation.parse(key.substring(1))), value);
                } else {
                    BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(key)).ifPresent(item -> items.put(item, value));
                }
            }
        }
        itemValues = Map.copyOf(items);
        tagValues = Map.copyOf(tags);
    }
}
