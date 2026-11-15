package net.per.primogemcraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class PGCConfig {
    public static final double DEFAULT_ZIPLINE_SPEED = 16.0D;
    public static final int DEFAULT_CAPTURING_RADIANCE_CHANCE = 25;
    public static final int DEFAULT_WEAPON_MAX_LEVEL = 90;
    public static final int DEFAULT_WEAPON_MAX_LEVEL_BONUS = 10;
    public static final int DEFAULT_WEAPON_LEVEL_XP_BASE = 10;
    public static final int DEFAULT_WEAPON_LEVEL_XP_GROWTH = 6;
    public static final int DEFAULT_WEAPON_DAMAGE_MULTIPLIER = 9;
    public static final int DEFAULT_ENHANCEMENT_ORE_XP = 20;
    public static final int DEFAULT_FINE_ENHANCEMENT_ORE_XP = 100;
    public static final int DEFAULT_MYSTIC_ENHANCEMENT_ORE_XP = 500;
    public static final int DEFAULT_EVENT_DROP_CHANCE = 1;
    public static final int DEFAULT_EVENT_WORLD_LIMIT = 5;
    public static final int DEFAULT_EVENT_RECOVERY_TICKS = 6000;
    public static final int DEFAULT_EVENT_ENTITY_LIFETIME = 6000;
    public static final int DEFAULT_EVENT_DROP_COOLDOWN = 20;
    public static final int DEFAULT_EVENT_CHALLENGE_TICKS = 3600;
    public static final int DEFAULT_ABUNDANCE_CHANCE = 25;
    public static final int DEFAULT_BLIGHT_ZOMBIE_CHANCE = 1;
    public static final int DEFAULT_MARA_HEALTH_THRESHOLD = 256;
    public static final double DEFAULT_GENSHINCRAFT_HEALTH_CONVERSION_SCALE = 0.025D;
    public static final int DEFAULT_GENSHINCRAFT_REGENERATION_SCALE = 32;
    public static final int DEFAULT_GENSHINCRAFT_BUBBLE_HEAL = 20;
    public static final int DEFAULT_GENSHINCRAFT_FREEZE_DAMAGE_SCALE = 25;
    public static final double DEFAULT_GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER = 5.0D;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("primogemcraft.json");
    private static final JsonObject DATA = load();

    public static final DoubleValue ZIPLINE_SPEED = new DoubleValue("zipline_speed_blocks_per_second", DEFAULT_ZIPLINE_SPEED, 0.1D, 100.0D);
    public static final IntValue CAPTURING_RADIANCE_CHANCE = new IntValue("capturing_radiance_chance", DEFAULT_CAPTURING_RADIANCE_CHANCE, 0, 100);
    public static final IntValue WEAPON_MAX_LEVEL = new IntValue("weapon_max_level", DEFAULT_WEAPON_MAX_LEVEL, 1, 1000);
    public static final IntValue WEAPON_MAX_LEVEL_BONUS = new IntValue("weapon_max_level_bonus", DEFAULT_WEAPON_MAX_LEVEL_BONUS, 0, 1000);
    public static final IntValue WEAPON_LEVEL_XP_BASE = new IntValue("weapon_level_xp_base", DEFAULT_WEAPON_LEVEL_XP_BASE, 1, 100000);
    public static final IntValue WEAPON_LEVEL_XP_GROWTH = new IntValue("weapon_level_xp_growth", DEFAULT_WEAPON_LEVEL_XP_GROWTH, 0, 100);
    public static final IntValue WEAPON_DAMAGE_MULTIPLIER = new IntValue("weapon_damage_multiplier", DEFAULT_WEAPON_DAMAGE_MULTIPLIER, 0, 1000);
    public static final IntValue ENHANCEMENT_ORE_XP = new IntValue("enhancement_ore_xp", DEFAULT_ENHANCEMENT_ORE_XP, 1, 100000);
    public static final IntValue FINE_ENHANCEMENT_ORE_XP = new IntValue("fine_enhancement_ore_xp", DEFAULT_FINE_ENHANCEMENT_ORE_XP, 1, 100000);
    public static final IntValue MYSTIC_ENHANCEMENT_ORE_XP = new IntValue("mystic_enhancement_ore_xp", DEFAULT_MYSTIC_ENHANCEMENT_ORE_XP, 1, 100000);
    public static final IntValue EVENT_DROP_CHANCE = new IntValue("event_drop_chance", DEFAULT_EVENT_DROP_CHANCE, 0, 100);
    public static final IntValue EVENT_WORLD_LIMIT = new IntValue("event_world_limit", DEFAULT_EVENT_WORLD_LIMIT, 0, 1000);
    public static final IntValue EVENT_RECOVERY_TICKS = new IntValue("event_recovery_ticks", DEFAULT_EVENT_RECOVERY_TICKS, 20, 240000);
    public static final IntValue EVENT_ENTITY_LIFETIME = new IntValue("event_entity_lifetime", DEFAULT_EVENT_ENTITY_LIFETIME, 100, 240000);
    public static final IntValue EVENT_DROP_COOLDOWN = new IntValue("event_drop_cooldown", DEFAULT_EVENT_DROP_COOLDOWN, 0, 1200);
    public static final IntValue EVENT_CHALLENGE_TICKS = new IntValue("event_challenge_ticks", DEFAULT_EVENT_CHALLENGE_TICKS, 100, 240000);
    public static final IntValue ABUNDANCE_CHANCE = new IntValue("abundance_chance", DEFAULT_ABUNDANCE_CHANCE, 0, 100);
    public static final IntValue BLIGHT_ZOMBIE_CHANCE = new IntValue("blight_zombie_chance", DEFAULT_BLIGHT_ZOMBIE_CHANCE, 0, 100);
    public static final IntValue MARA_HEALTH_THRESHOLD = new IntValue("mara_health_threshold", DEFAULT_MARA_HEALTH_THRESHOLD, 0, 100000);
    public static final IntListValue WEAPON_LEVEL_XP_REQUIREMENTS = new IntListValue("weapon_level_xp_requirements");
    public static final DoubleValue GENSHINCRAFT_HEALTH_CONVERSION_SCALE = new DoubleValue("genshincraft_health_conversion_scale", DEFAULT_GENSHINCRAFT_HEALTH_CONVERSION_SCALE, 0.0D, 1000.0D);
    public static final IntValue GENSHINCRAFT_REGENERATION_SCALE = new IntValue("genshincraft_regeneration_scale", DEFAULT_GENSHINCRAFT_REGENERATION_SCALE, 0, 1000);
    public static final IntValue GENSHINCRAFT_BUBBLE_HEAL = new IntValue("genshincraft_bubble_heal", DEFAULT_GENSHINCRAFT_BUBBLE_HEAL, 0, 100000);
    public static final IntValue GENSHINCRAFT_FREEZE_DAMAGE_SCALE = new IntValue("genshincraft_freeze_damage_scale", DEFAULT_GENSHINCRAFT_FREEZE_DAMAGE_SCALE, 0, 1000);
    public static final DoubleValue GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER = new DoubleValue("genshincraft_geo_weakness_amplifier", DEFAULT_GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER, 0.0D, 1000.0D);

    public static final Spec SPEC = new Spec();

    private PGCConfig() {
    }

    private static JsonObject load() {
        if (Files.exists(FILE)) {
            try (var reader = Files.newBufferedReader(FILE)) {
                var element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) return element.getAsJsonObject();
            } catch (IOException ignored) {
            }
        }
        return new JsonObject();
    }

    public static final class Spec {
        public void save() {
            try {
                Files.createDirectories(FILE.getParent());
                Files.writeString(FILE, GSON.toJson(DATA));
            } catch (IOException ignored) {
            }
        }
    }

    public static final class IntValue implements Supplier<Integer> {
        private final String key;
        private final int defaultValue;
        private final int min;
        private final int max;

        IntValue(String key, int defaultValue, int min, int max) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
        }

        @Override
        public Integer get() {
            if (DATA.has(key)) return DATA.get(key).getAsInt();
            return defaultValue;
        }

        public void set(int value) {
            DATA.addProperty(key, Math.max(min, Math.min(max, value)));
        }
    }

    public static final class DoubleValue implements Supplier<Double> {
        private final String key;
        private final double defaultValue;
        private final double min;
        private final double max;

        DoubleValue(String key, double defaultValue, double min, double max) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
        }

        @Override
        public Double get() {
            if (DATA.has(key)) return DATA.get(key).getAsDouble();
            return defaultValue;
        }

        public void set(double value) {
            DATA.addProperty(key, Math.max(min, Math.min(max, value)));
        }
    }

    public static final class IntListValue implements Supplier<List<? extends Integer>> {
        private final String key;

        IntListValue(String key) {
            this.key = key;
        }

        @Override
        public List<? extends Integer> get() {
            var result = new ArrayList<Integer>();
            if (DATA.has(key) && DATA.get(key).isJsonArray()) {
                for (var element : DATA.getAsJsonArray(key)) {
                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
                        var value = element.getAsInt();
                        if (value > 0) result.add(value);
                    }
                }
            }
            return result;
        }

        public void set(List<Integer> values) {
            var array = new com.google.gson.JsonArray();
            for (var value : values) array.add(value);
            DATA.add(key, array);
        }
    }
}