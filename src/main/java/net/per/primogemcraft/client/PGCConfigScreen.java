package net.per.primogemcraft.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.per.primogemcraft.config.PGCConfig;

import java.util.function.Consumer;

public final class PGCConfigScreen {
    private static final String CAPTURING_RADIANCE_CHANCE_KEY = "primogemcraft.configuration.capturing_radiance_chance";
    private static final String WEAPON_MAX_LEVEL_KEY = "primogemcraft.configuration.weapon_max_level";
    private static final String WEAPON_MAX_LEVEL_BONUS_KEY = "primogemcraft.configuration.weapon_max_level_bonus";
    private static final String WEAPON_LEVEL_XP_BASE_KEY = "primogemcraft.configuration.weapon_level_xp_base";
    private static final String WEAPON_LEVEL_XP_GROWTH_KEY = "primogemcraft.configuration.weapon_level_xp_growth";
    private static final String WEAPON_DAMAGE_MULTIPLIER_KEY = "primogemcraft.configuration.weapon_damage_multiplier";
    private static final String ENHANCEMENT_ORE_XP_KEY = "primogemcraft.configuration.enhancement_ore_xp";
    private static final String FINE_ENHANCEMENT_ORE_XP_KEY = "primogemcraft.configuration.fine_enhancement_ore_xp";
    private static final String MYSTIC_ENHANCEMENT_ORE_XP_KEY = "primogemcraft.configuration.mystic_enhancement_ore_xp";
    private static final String EVENT_DROP_CHANCE_KEY = "primogemcraft.configuration.event_drop_chance";
    private static final String EVENT_WORLD_LIMIT_KEY = "primogemcraft.configuration.event_world_limit";
    private static final String EVENT_RECOVERY_TICKS_KEY = "primogemcraft.configuration.event_recovery_ticks";
    private static final String EVENT_ENTITY_LIFETIME_KEY = "primogemcraft.configuration.event_entity_lifetime";
    private static final String EVENT_DROP_COOLDOWN_KEY = "primogemcraft.configuration.event_drop_cooldown";
    private static final String EVENT_CHALLENGE_TICKS_KEY = "primogemcraft.configuration.event_challenge_ticks";
    private static final String ABUNDANCE_CHANCE_KEY = "primogemcraft.configuration.abundance_chance";
    private static final String BLIGHT_ZOMBIE_CHANCE_KEY = "primogemcraft.configuration.blight_zombie_chance";
    private static final String MARA_HEALTH_THRESHOLD_KEY = "primogemcraft.configuration.mara_health_threshold";
    private static final String GENSHINCRAFT_HEALTH_CONVERSION_SCALE_KEY = "primogemcraft.configuration.genshincraft_health_conversion_scale";
    private static final String GENSHINCRAFT_REGENERATION_SCALE_KEY = "primogemcraft.configuration.genshincraft_regeneration_scale";
    private static final String GENSHINCRAFT_BUBBLE_HEAL_KEY = "primogemcraft.configuration.genshincraft_bubble_heal";
    private static final String GENSHINCRAFT_FREEZE_DAMAGE_SCALE_KEY = "primogemcraft.configuration.genshincraft_freeze_damage_scale";
    private static final String GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER_KEY = "primogemcraft.configuration.genshincraft_geo_weakness_amplifier";
    private static final int MAX_LEVEL_LIMIT = 1000;
    private static final int MAX_XP_LIMIT = 100000;
    private static final int MAX_MULTIPLIER_LIMIT = 1000;
    private static final int MAX_HEALTH_LIMIT = 100000;
    private static final int MAX_GROWTH_LIMIT = 100;
    private static final int MAX_CHANCE_LIMIT = 100;
    private static final int MAX_TICKS_LIMIT = 240000;
    private static final int MAX_COOLDOWN_LIMIT = 1200;
    private static final int MIN_TICKS = 20;
    private static final int MIN_ENTITY_LIFETIME = 100;
    private static final int ZERO = 0;
    private static final double MIN_MULTIPLIER = 0.0D;
    private static final double MAX_MULTIPLIER = 1000.0D;

    private PGCConfigScreen() {
    }

    public static Screen create(ModContainer container, Screen parent) {
        var builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.primogemcraft.title"));
        var wish = builder.getOrCreateCategory(Component.translatable("config.primogemcraft.category.wish"));
        wish.addEntry(intEntry(builder, CAPTURING_RADIANCE_CHANCE_KEY, PGCConfig.CAPTURING_RADIANCE_CHANCE.get(), PGCConfig.DEFAULT_CAPTURING_RADIANCE_CHANCE, 0, MAX_CHANCE_LIMIT, PGCConfig.CAPTURING_RADIANCE_CHANCE::set));
        var weapon = builder.getOrCreateCategory(Component.translatable("config.primogemcraft.category.weapon"));
        weapon.addEntry(intEntry(builder, WEAPON_MAX_LEVEL_KEY, PGCConfig.WEAPON_MAX_LEVEL.get(), PGCConfig.DEFAULT_WEAPON_MAX_LEVEL, 1, MAX_LEVEL_LIMIT, PGCConfig.WEAPON_MAX_LEVEL::set));
        weapon.addEntry(intEntry(builder, WEAPON_MAX_LEVEL_BONUS_KEY, PGCConfig.WEAPON_MAX_LEVEL_BONUS.get(), PGCConfig.DEFAULT_WEAPON_MAX_LEVEL_BONUS, 0, MAX_LEVEL_LIMIT, PGCConfig.WEAPON_MAX_LEVEL_BONUS::set));
        weapon.addEntry(intEntry(builder, WEAPON_LEVEL_XP_BASE_KEY, PGCConfig.WEAPON_LEVEL_XP_BASE.get(), PGCConfig.DEFAULT_WEAPON_LEVEL_XP_BASE, 1, MAX_XP_LIMIT, PGCConfig.WEAPON_LEVEL_XP_BASE::set));
        weapon.addEntry(intEntry(builder, WEAPON_LEVEL_XP_GROWTH_KEY, PGCConfig.WEAPON_LEVEL_XP_GROWTH.get(), PGCConfig.DEFAULT_WEAPON_LEVEL_XP_GROWTH, 0, MAX_GROWTH_LIMIT, PGCConfig.WEAPON_LEVEL_XP_GROWTH::set));
        weapon.addEntry(intEntry(builder, ENHANCEMENT_ORE_XP_KEY, PGCConfig.ENHANCEMENT_ORE_XP.get(), PGCConfig.DEFAULT_ENHANCEMENT_ORE_XP, 1, MAX_XP_LIMIT, PGCConfig.ENHANCEMENT_ORE_XP::set));
        weapon.addEntry(intEntry(builder, FINE_ENHANCEMENT_ORE_XP_KEY, PGCConfig.FINE_ENHANCEMENT_ORE_XP.get(), PGCConfig.DEFAULT_FINE_ENHANCEMENT_ORE_XP, 1, MAX_XP_LIMIT, PGCConfig.FINE_ENHANCEMENT_ORE_XP::set));
        weapon.addEntry(intEntry(builder, MYSTIC_ENHANCEMENT_ORE_XP_KEY, PGCConfig.MYSTIC_ENHANCEMENT_ORE_XP.get(), PGCConfig.DEFAULT_MYSTIC_ENHANCEMENT_ORE_XP, 1, MAX_XP_LIMIT, PGCConfig.MYSTIC_ENHANCEMENT_ORE_XP::set));
        weapon.addEntry(intEntry(builder, WEAPON_DAMAGE_MULTIPLIER_KEY, PGCConfig.WEAPON_DAMAGE_MULTIPLIER.get(), PGCConfig.DEFAULT_WEAPON_DAMAGE_MULTIPLIER, 0, MAX_LEVEL_LIMIT, PGCConfig.WEAPON_DAMAGE_MULTIPLIER::set));
        var event = builder.getOrCreateCategory(Component.translatable("config.primogemcraft.category.event"));
        event.addEntry(intEntry(builder, EVENT_DROP_CHANCE_KEY, PGCConfig.EVENT_DROP_CHANCE.get(), PGCConfig.DEFAULT_EVENT_DROP_CHANCE, ZERO, MAX_CHANCE_LIMIT, PGCConfig.EVENT_DROP_CHANCE::set));
        event.addEntry(intEntry(builder, EVENT_WORLD_LIMIT_KEY, PGCConfig.EVENT_WORLD_LIMIT.get(), PGCConfig.DEFAULT_EVENT_WORLD_LIMIT, ZERO, MAX_LEVEL_LIMIT, PGCConfig.EVENT_WORLD_LIMIT::set));
        event.addEntry(intEntry(builder, EVENT_RECOVERY_TICKS_KEY, PGCConfig.EVENT_RECOVERY_TICKS.get(), PGCConfig.DEFAULT_EVENT_RECOVERY_TICKS, MIN_TICKS, MAX_TICKS_LIMIT, PGCConfig.EVENT_RECOVERY_TICKS::set));
        event.addEntry(intEntry(builder, EVENT_ENTITY_LIFETIME_KEY, PGCConfig.EVENT_ENTITY_LIFETIME.get(), PGCConfig.DEFAULT_EVENT_ENTITY_LIFETIME, MIN_ENTITY_LIFETIME, MAX_TICKS_LIMIT, PGCConfig.EVENT_ENTITY_LIFETIME::set));
        event.addEntry(intEntry(builder, EVENT_DROP_COOLDOWN_KEY, PGCConfig.EVENT_DROP_COOLDOWN.get(), PGCConfig.DEFAULT_EVENT_DROP_COOLDOWN, ZERO, MAX_COOLDOWN_LIMIT, PGCConfig.EVENT_DROP_COOLDOWN::set));
        event.addEntry(intEntry(builder, EVENT_CHALLENGE_TICKS_KEY, PGCConfig.EVENT_CHALLENGE_TICKS.get(), PGCConfig.DEFAULT_EVENT_CHALLENGE_TICKS, MIN_ENTITY_LIFETIME, MAX_TICKS_LIMIT, PGCConfig.EVENT_CHALLENGE_TICKS::set));
        var other = builder.getOrCreateCategory(Component.translatable("config.primogemcraft.category.other"));
        var ziplineSpeedKey = "primogemcraft.configuration.zipline_speed_blocks_per_second";
        other.addEntry(builder.entryBuilder().startDoubleField(Component.translatable(ziplineSpeedKey), PGCConfig.ZIPLINE_SPEED.get())
                .setDefaultValue(PGCConfig.DEFAULT_ZIPLINE_SPEED).setMin(0.1D).setMax(100.0D)
                .setTooltip(Component.translatable(ziplineSpeedKey + ".tooltip"))
                .setSaveConsumer(PGCConfig.ZIPLINE_SPEED::set).build());
        other.addEntry(intEntry(builder, ABUNDANCE_CHANCE_KEY, PGCConfig.ABUNDANCE_CHANCE.get(), PGCConfig.DEFAULT_ABUNDANCE_CHANCE, ZERO, MAX_CHANCE_LIMIT, PGCConfig.ABUNDANCE_CHANCE::set));
        other.addEntry(intEntry(builder, BLIGHT_ZOMBIE_CHANCE_KEY, PGCConfig.BLIGHT_ZOMBIE_CHANCE.get(), PGCConfig.DEFAULT_BLIGHT_ZOMBIE_CHANCE, ZERO, MAX_CHANCE_LIMIT, PGCConfig.BLIGHT_ZOMBIE_CHANCE::set));
        other.addEntry(intEntry(builder, MARA_HEALTH_THRESHOLD_KEY, PGCConfig.MARA_HEALTH_THRESHOLD.get(), PGCConfig.DEFAULT_MARA_HEALTH_THRESHOLD, ZERO, MAX_HEALTH_LIMIT, PGCConfig.MARA_HEALTH_THRESHOLD::set));
        if (ModList.get().isLoaded("genshincraft")) {
            var genshincraft = builder.getOrCreateCategory(Component.translatable("config.primogemcraft.category.genshincraft"));
            genshincraft.addEntry(doubleEntry(builder, GENSHINCRAFT_HEALTH_CONVERSION_SCALE_KEY, PGCConfig.GENSHINCRAFT_HEALTH_CONVERSION_SCALE.get(), PGCConfig.DEFAULT_GENSHINCRAFT_HEALTH_CONVERSION_SCALE, PGCConfig.GENSHINCRAFT_HEALTH_CONVERSION_SCALE::set));
            genshincraft.addEntry(intEntry(builder, GENSHINCRAFT_REGENERATION_SCALE_KEY, PGCConfig.GENSHINCRAFT_REGENERATION_SCALE.get(), PGCConfig.DEFAULT_GENSHINCRAFT_REGENERATION_SCALE, ZERO, MAX_MULTIPLIER_LIMIT, PGCConfig.GENSHINCRAFT_REGENERATION_SCALE::set));
            genshincraft.addEntry(intEntry(builder, GENSHINCRAFT_BUBBLE_HEAL_KEY, PGCConfig.GENSHINCRAFT_BUBBLE_HEAL.get(), PGCConfig.DEFAULT_GENSHINCRAFT_BUBBLE_HEAL, ZERO, MAX_HEALTH_LIMIT, PGCConfig.GENSHINCRAFT_BUBBLE_HEAL::set));
            genshincraft.addEntry(intEntry(builder, GENSHINCRAFT_FREEZE_DAMAGE_SCALE_KEY, PGCConfig.GENSHINCRAFT_FREEZE_DAMAGE_SCALE.get(), PGCConfig.DEFAULT_GENSHINCRAFT_FREEZE_DAMAGE_SCALE, ZERO, MAX_MULTIPLIER_LIMIT, PGCConfig.GENSHINCRAFT_FREEZE_DAMAGE_SCALE::set));
            genshincraft.addEntry(doubleEntry(builder, GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER_KEY, PGCConfig.GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER.get(), PGCConfig.DEFAULT_GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER, PGCConfig.GENSHINCRAFT_GEO_WEAKNESS_AMPLIFIER::set));
        }
        builder.setSavingRunnable(PGCConfig.SPEC::save);
        return builder.build();
    }

    private static IntegerListEntry intEntry(ConfigBuilder builder, String key, int value, int defaultValue, int min, int max, Consumer<Integer> save) {
        return builder.entryBuilder()
                .startIntField(Component.translatable(key), value)
                .setDefaultValue(defaultValue)
                .setMin(min)
                .setMax(max)
                .setTooltip(Component.translatable(key + ".tooltip"))
                .setSaveConsumer(save)
                .build();
    }

    private static DoubleListEntry doubleEntry(ConfigBuilder builder, String key, double value, double defaultValue, Consumer<Double> save) {
        return builder.entryBuilder()
                .startDoubleField(Component.translatable(key), value)
                .setDefaultValue(defaultValue)
                .setMin(MIN_MULTIPLIER)
                .setMax(MAX_MULTIPLIER)
                .setTooltip(Component.translatable(key + ".tooltip"))
                .setSaveConsumer(save)
                .build();
    }
}
