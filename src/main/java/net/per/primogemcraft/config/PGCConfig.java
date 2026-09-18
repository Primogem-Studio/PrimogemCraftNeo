package net.per.primogemcraft.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class PGCConfig {
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
    public static final int DEFAULT_MARA_SPAWN_MULTIPLIER = 1;
    public static final int DEFAULT_MARA_HEALTH_THRESHOLD = 256;
    public static final double DEFAULT_GENSHINCRAFT_HEALTH_CONVERSION_SCALE = 0.025D;
    public static final int DEFAULT_GENSHINCRAFT_REGENERATION_SCALE = 32;
    public static final int DEFAULT_GENSHINCRAFT_BUBBLE_HEAL = 20;
    public static final int DEFAULT_GENSHINCRAFT_FREEZE_DAMAGE_SCALE = 25;
    public static final double DEFAULT_GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER = 5.0D;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CAPTURING_RADIANCE_CHANCE = BUILDER.defineInRange("capturing_radiance_chance", DEFAULT_CAPTURING_RADIANCE_CHANCE, 0, 100);
    public static final ModConfigSpec.IntValue WEAPON_MAX_LEVEL = BUILDER.defineInRange("weapon_max_level", DEFAULT_WEAPON_MAX_LEVEL, 1, 1000);
    public static final ModConfigSpec.IntValue WEAPON_MAX_LEVEL_BONUS = BUILDER.defineInRange("weapon_max_level_bonus", DEFAULT_WEAPON_MAX_LEVEL_BONUS, 0, 1000);
    public static final ModConfigSpec.IntValue WEAPON_LEVEL_XP_BASE = BUILDER.defineInRange("weapon_level_xp_base", DEFAULT_WEAPON_LEVEL_XP_BASE, 1, 100000);
    public static final ModConfigSpec.IntValue WEAPON_LEVEL_XP_GROWTH = BUILDER.defineInRange("weapon_level_xp_growth", DEFAULT_WEAPON_LEVEL_XP_GROWTH, 0, 100);
    public static final ModConfigSpec.IntValue WEAPON_DAMAGE_MULTIPLIER = BUILDER.defineInRange("weapon_damage_multiplier", DEFAULT_WEAPON_DAMAGE_MULTIPLIER, 0, 1000);
    public static final ModConfigSpec.IntValue ENHANCEMENT_ORE_XP = BUILDER.defineInRange("enhancement_ore_xp", DEFAULT_ENHANCEMENT_ORE_XP, 1, 100000);
    public static final ModConfigSpec.IntValue FINE_ENHANCEMENT_ORE_XP = BUILDER.defineInRange("fine_enhancement_ore_xp", DEFAULT_FINE_ENHANCEMENT_ORE_XP, 1, 100000);
    public static final ModConfigSpec.IntValue MYSTIC_ENHANCEMENT_ORE_XP = BUILDER.defineInRange("mystic_enhancement_ore_xp", DEFAULT_MYSTIC_ENHANCEMENT_ORE_XP, 1, 100000);
    public static final ModConfigSpec.IntValue EVENT_DROP_CHANCE = BUILDER.defineInRange("event_drop_chance", DEFAULT_EVENT_DROP_CHANCE, 0, 100);
    public static final ModConfigSpec.IntValue EVENT_WORLD_LIMIT = BUILDER.defineInRange("event_world_limit", DEFAULT_EVENT_WORLD_LIMIT, 0, 1000);
    public static final ModConfigSpec.IntValue EVENT_RECOVERY_TICKS = BUILDER.defineInRange("event_recovery_ticks", DEFAULT_EVENT_RECOVERY_TICKS, 20, 240000);
    public static final ModConfigSpec.IntValue EVENT_ENTITY_LIFETIME = BUILDER.defineInRange("event_entity_lifetime", DEFAULT_EVENT_ENTITY_LIFETIME, 100, 240000);
    public static final ModConfigSpec.IntValue EVENT_DROP_COOLDOWN = BUILDER.defineInRange("event_drop_cooldown", DEFAULT_EVENT_DROP_COOLDOWN, 0, 1200);
    public static final ModConfigSpec.IntValue EVENT_CHALLENGE_TICKS = BUILDER.defineInRange("event_challenge_ticks", DEFAULT_EVENT_CHALLENGE_TICKS, 100, 240000);
    public static final ModConfigSpec.IntValue MARA_SPAWN_MULTIPLIER = BUILDER.defineInRange("mara_spawn_multiplier", DEFAULT_MARA_SPAWN_MULTIPLIER, 0, 1000);
    public static final ModConfigSpec.IntValue MARA_HEALTH_THRESHOLD = BUILDER.defineInRange("mara_health_threshold", DEFAULT_MARA_HEALTH_THRESHOLD, 0, 100000);

    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> WEAPON_LEVEL_XP_REQUIREMENTS = BUILDER
            .comment("Per-level weapon experience requirement, entry N is the experience needed to go from level N to level N + 1.",
                    "Leave it empty to derive every entry from weapon_level_xp_base and weapon_level_xp_growth.")
            .defineListAllowEmpty("weapon_level_xp_requirements", List.of(), () -> DEFAULT_WEAPON_LEVEL_XP_BASE, value -> value instanceof Integer amount && amount > 0);

    public static final ModConfigSpec.DoubleValue GENSHINCRAFT_HEALTH_CONVERSION_SCALE = BUILDER
            .comment("GenshinCraft only. Ratio the max-health-to-attack conversion of Staff of Homa and Primordial Jade Cutter is multiplied with;",
                    "without GenshinCraft the weapons use the ratio their description shows.")
            .defineInRange("genshincraft_health_conversion_scale", DEFAULT_GENSHINCRAFT_HEALTH_CONVERSION_SCALE, 0.0D, 1000.0D);
    public static final ModConfigSpec.IntValue GENSHINCRAFT_REGENERATION_SCALE = BUILDER
            .comment("GenshinCraft only. Regeneration and wither level Splendor of Tranquil Waters applies per refinement layer.")
            .defineInRange("genshincraft_regeneration_scale", DEFAULT_GENSHINCRAFT_REGENERATION_SCALE, 0, 1000);
    public static final ModConfigSpec.IntValue GENSHINCRAFT_BUBBLE_HEAL = BUILDER
            .comment("GenshinCraft only. Health Cleansing Bubble restores per tick.")
            .defineInRange("genshincraft_bubble_heal", DEFAULT_GENSHINCRAFT_BUBBLE_HEAL, 0, 100000);
    public static final ModConfigSpec.IntValue GENSHINCRAFT_FREEZE_DAMAGE_SCALE = BUILDER
            .comment("GenshinCraft only. Damage Persistent Freeze deals every 10 ticks per amplifier.")
            .defineInRange("genshincraft_freeze_damage_scale", DEFAULT_GENSHINCRAFT_FREEZE_DAMAGE_SCALE, 0, 1000);
    public static final ModConfigSpec.DoubleValue GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER = BUILDER
            .comment("GenshinCraft only. Weakness level the geo form of Philosophies of Prosperity applies;",
                    "without GenshinCraft the weapon applies the level its description shows.")
            .defineInRange("genshincraft_geo_weakness_amplifier", DEFAULT_GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER, 0.0D, 1000.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private PGCConfig() {
    }
}
