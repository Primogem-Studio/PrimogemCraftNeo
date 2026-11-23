package net.per.primogemcraft.system.wish;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

record WishLootChances(Function<String, JsonObject> tables, Function<String, List<String>> tags) {
    Map<String, Double> forTable(String id) {
        var path = new HashSet<String>();
        var result = referencedTable(id, path, "neoforge:unknown_loot_table");
        result.values().removeIf(chance -> chance == 0);
        return result;
    }

    private Map<String, Double> referencedTable(String id, Set<String> path, String queriedId) {
        if (!path.add(id)) return Map.of();
        var result = table(tables.apply(id), path, queriedId);
        path.remove(id);
        return result;
    }

    private Map<String, Double> table(JsonObject table, Set<String> path, String queriedId) {
        var result = new LinkedHashMap<String, Double>();
        for (var element : array(table, "pools")) {
            var pool = element.getAsJsonObject();
            var active = conditions(pool, queriedId);
            if (active == 0) continue;
            perRoll(pool, path, queriedId).forEach((item, hit) ->
                    result.merge(item, active * rolledChance(hit, pool.get("rolls")), WishLootChances::either));
        }
        return result;
    }

    private Map<String, Double> perRoll(JsonObject pool, Set<String> path, String queriedId) {
        var weights = new LinkedHashMap<String, Double>();
        var guaranteedWeights = new LinkedHashMap<String, Double>();
        var total = 0.0;
        var guaranteedTotal = 0.0;
        var gate = 1.0;
        for (var value : array(pool, "entries")) {
            var entry = value.getAsJsonObject();
            var weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
            var eligible = conditions(entry, queriedId);
            if (weight <= 0 || eligible == 0) continue;
            var items = entryItems(entry, path, queriedId);
            var slots = switch (entry.get("type").getAsString()) {
                case "minecraft:item", "minecraft:loot_table", "minecraft:empty", "minecraft:dynamic" -> 1.0;
                case "minecraft:tag" -> entry.get("expand").getAsBoolean() ? (double) items.size() : 1.0;
                default -> Double.NaN;
            };
            total += weight * slots;
            items.forEach((item, hit) -> weights.merge(item, weight * hit, Double::sum));
            if (eligible == 1) {
                guaranteedTotal += weight * slots;
                items.forEach((item, hit) -> guaranteedWeights.merge(item, weight * hit, Double::sum));
            } else {
                gate = gate == 1 ? eligible : Double.NaN;
            }
        }
        for (var item : weights.entrySet()) {
            var enabled = item.getValue() / total;
            var disabled = guaranteedTotal == 0 ? 0 : guaranteedWeights.getOrDefault(item.getKey(), 0.0) / guaranteedTotal;
            item.setValue(gate == 1 ? enabled : gate * enabled + (1 - gate) * disabled);
        }
        return weights;
    }

    private Map<String, Double> entryItems(JsonObject entry, Set<String> path, String queriedId) {
        var items = new LinkedHashMap<String, Double>();
        switch (entry.get("type").getAsString()) {
            case "minecraft:item" -> items.put(entry.get("name").getAsString(), 1.0);
            case "minecraft:tag" -> tags.apply(entry.get("name").getAsString()).forEach(item -> items.put(item, 1.0));
            case "minecraft:loot_table" -> {
                var value = entry.get("value");
                return value.isJsonObject() ? table(value.getAsJsonObject(), path, queriedId) : referencedTable(value.getAsString(), path, queriedId);
            }
            case "minecraft:alternatives", "minecraft:group", "minecraft:sequence" -> {
                for (var child : array(entry, "children")) {
                    entryItems(child.getAsJsonObject(), path, queriedId).keySet().forEach(item -> items.put(item, Double.NaN));
                }
            }
        }
        return items;
    }

    private double conditions(JsonObject object, String tableId) {
        return allOf(array(object, "conditions"), tableId);
    }

    private double allOf(JsonArray terms, String tableId) {
        var result = 1.0;
        for (var term : terms) {
            var chance = conditionChance(term.getAsJsonObject(), tableId);
            if (chance == 0) return 0;
            result *= chance;
        }
        return result;
    }

    private double conditionChance(JsonObject condition, String tableId) {
        return switch (condition.get("condition").getAsString()) {
            case "neoforge:loot_table_id" -> condition.get("loot_table_id").getAsString().equals(tableId) ? 1 : 0;
            case MOD_ID + ":is_chest" -> {
                if (condition.has("external_only") && condition.get("external_only").getAsBoolean() && tableId.startsWith(MOD_ID + ":")) yield 0;
                var table = tables.apply(tableId);
                yield table != null && table.has("type") && table.get("type").getAsString().equals("minecraft:chest") ? 1 : 0;
            }
            case "minecraft:random_chance" -> Math.clamp(constant(condition.get("chance")), 0, 1);
            case "minecraft:all_of" -> allOf(condition.getAsJsonArray("terms"), tableId);
            case "minecraft:any_of" -> {
                var chance = 0.0;
                for (var term : condition.getAsJsonArray("terms")) {
                    chance = either(chance, conditionChance(term.getAsJsonObject(), tableId));
                    if (chance == 1) break;
                }
                yield chance;
            }
            case "minecraft:inverted" -> 1 - conditionChance(condition.getAsJsonObject("term"), tableId);
            default -> Double.NaN;
        };
    }

    private static double rolledChance(double hit, JsonElement rolls) {
        if (rolls == null) return Double.NaN;
        var min = constant(rolls);
        var max = min;
        if (rolls.isJsonObject() && (!rolls.getAsJsonObject().has("type")
                || rolls.getAsJsonObject().get("type").getAsString().equals("minecraft:uniform"))) {
            var range = rolls.getAsJsonObject();
            min = constant(range.get("min"));
            max = constant(range.get("max"));
        }
        if (!Double.isFinite(min) || !Double.isFinite(max)) return Double.NaN;
        min = Math.floor(min);
        max = Math.max(min, Math.floor(max));
        if (max <= 0 || hit == 0) return 0;
        if (min == 1 && max == 1) return hit;
        if (min == max) return -Math.expm1(min * Math.log1p(-hit));
        if (min == 0 && max == 1) return hit / 2;
        var outcomes = max - min + 1;
        var first = Math.max(1, min);
        var positive = max - first + 1;
        if (hit == 1) return positive / outcomes;
        var logMiss = Math.log1p(-hit);
        var misses = Math.exp(first * logMiss) * -Math.expm1(positive * logMiss) / hit;
        return Math.clamp((positive - misses) / outcomes, 0, 1);
    }

    private static double constant(JsonElement value) {
        return value != null && value.isJsonPrimitive() ? value.getAsDouble() : Double.NaN;
    }

    private static double either(double first, double second) {
        if (first == 1 || second == 1) return 1;
        return first + (1 - first) * second;
    }

    private static JsonArray array(JsonObject object, String name) {
        return object.has(name) ? object.getAsJsonArray(name) : new JsonArray();
    }
}
