package net.per.primogemcraft.collab.teyvatdelight;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

final class CurrencyExchange extends SimpleJsonResourceReloadListener {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "currency_exchange");
    private static List<Trade> trades = List.of();
    private static long revision;

    CurrencyExchange() {
        super(new Gson(), "teyvat_exchange");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        var definition = resources.get(ID);
        if (definition == null) throw new IllegalArgumentException("Missing currency exchange definition: " + ID);
        trades = parse(definition.getAsJsonObject());
        revision++;
    }

    static List<Trade> parse(JsonObject root) {
        var enabled = GsonHelper.getAsBoolean(root, "enabled", true);
        var result = new ArrayList<Trade>();
        for (var id : List.of("craft_to_teyvat_primogem", "teyvat_to_craft_primogem", "craft_to_teyvat_mora", "teyvat_to_craft_mora")) {
            var trade = GsonHelper.getAsJsonObject(root, id);
            result.add(new Trade(number(trade, "cost", 1, 64), number(trade, "reward", 1, 64),
                    number(trade, "daily_limit", -1, 1000000), enabled && GsonHelper.getAsBoolean(trade, "enabled", true)));
        }
        return List.copyOf(result);
    }

    private static int number(JsonObject value, String key, int minimum, int maximum) {
        var number = value.getAsJsonPrimitive(key);
        if (number == null || !number.isNumber()) throw new IllegalArgumentException("Expected integer: " + key);
        var result = number.getAsBigDecimal().intValueExact();
        if (result < minimum || result > maximum) throw new IllegalArgumentException("Invalid currency exchange " + key + ": " + result);
        return result;
    }

    static List<Trade> trades() {
        return trades;
    }

    static long revision() {
        return revision;
    }

    record Trade(int cost, int reward, int limit, boolean enabled) {
    }
}
