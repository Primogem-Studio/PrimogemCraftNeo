package net.per.primogemcraft.system.wish;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishLootChancesTest {
    public static void main(String[] args) throws Exception {
        var tables = new HashMap<String, JsonObject>();
        var tags = new HashMap<String, List<String>>();
        var data = Path.of("src/main/resources/data/primogemcraft");
        var chances = new WishLootChances(tables::get, tag -> tags.getOrDefault(tag, List.of()));
        var restored = List.of("primogem", "enigmata_gift_box", "enhancement_ore", "fine_enhancement_ore",
                "adventurers_experience_reward", "wanderers_advice_reward", "acquaint_fate", "intertwined_fate",
                "wish_core", "unidentified_doll", "footprints_of_fate", "lucky_special_ticket", "colorful_sunglasses");
        for (var id : List.of("wish/blue", "wish/purple", "wish/gold", "chests/mora_ruins", "chests/trash_chest")) {
            var table = json(Files.readString(data.resolve("loot_table/" + id + ".json")));
            tables.put(id, table);
            var pool = table.getAsJsonArray("pools").get(0).getAsJsonObject();
            var entries = pool.getAsJsonArray("entries").asList();
            var names = entries.stream().map(entry -> entry.getAsJsonObject().get("name").getAsString()).toList();
            for (var item : restored) {
                if (names.stream().filter(name -> name.equals("primogemcraft:" + item)).count() != 1) {
                    throw new AssertionError(id + " missing or duplicate " + item);
                }
            }
            if (!id.startsWith("wish/")) continue;
            if (pool.get("rolls").getAsInt() != 1) throw new AssertionError(id);
            var tag = "primogemcraft:" + id;
            var tagFile = json(Files.readString(data.resolve("tags/item/" + id + ".json")));
            if (!tagFile.getAsJsonArray("values").isEmpty()) throw new AssertionError(tag);
            var weight = entries.stream().map(entry -> entry.getAsJsonObject())
                    .filter(entry -> entry.get("type").getAsString().equals("minecraft:item"))
                    .mapToInt(entry -> entry.has("weight") ? entry.get("weight").getAsInt() : 1).sum();
            var base = chances.forTable(id);
            if (base.containsKey("example:reward") || base.values().stream().anyMatch(value -> !Double.isFinite(value))) {
                throw new AssertionError(id);
            }
            check(base, "primogemcraft:enigmata_gift_box", 3.0 / weight);
            tags.put(tag, List.of("example:reward", "example:another_reward"));
            var expanded = chances.forTable(id);
            check(expanded, "example:reward", 1.0 / (weight + 2));
            check(expanded, "example:another_reward", 1.0 / (weight + 2));
            check(expanded, "primogemcraft:enigmata_gift_box", 3.0 / (weight + 2));
            tags.clear();
        }
        check(chances.forTable("wish/purple"), "primogemcraft:masterless_stardust", 0.5);
        check(chances.forTable("wish/purple"), "primogemcraft:masterless_starglitter", 1);
        check(chances.forTable("wish/gold"), "primogemcraft:masterless_starglitter", 1);

        tables.put("extra", json("""
                {"pools":[{"rolls":2,"entries":[{"type":"minecraft:tag","name":"gems","expand":true},
                {"type":"minecraft:empty","weight":2}]}]}
                """));
        tables.put("nested", json("""
                {"pools":[{"rolls":1,"entries":[{"type":"minecraft:loot_table","value":"extra"}],
                "conditions":[{"condition":"minecraft:random_chance","chance":0.5}]}]}
                """));
        tags.put("gems", List.of("gem", "ore"));
        check(chances.forTable("nested"), "gem", 0.5 * (1 - 0.75 * 0.75));
        var pool = tables.get("nested").getAsJsonArray("pools").get(0).getAsJsonObject();
        pool.getAsJsonArray("conditions").add(json("""
                {"condition":"minecraft:weather_check","raining":true}
                """));
        if (!Double.isNaN(chances.forTable("nested").get("gem"))) throw new AssertionError();
    }

    private static JsonObject json(String source) {
        return JsonParser.parseString(source).getAsJsonObject();
    }

    private static void check(Map<String, Double> chances, String item, double expected) {
        var actual = chances.get(item);
        if (actual == null || !(Math.abs(actual - expected) < 1e-12)) {
            throw new AssertionError(item + ": " + actual + " != " + expected);
        }
    }
}
