package net.per.primogemcraft.compat.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.function.Predicate;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * Fabric 等价实现 Forge 的 {@code forge:add_features} biome modifier。
 * 原 JSON 位于 data/primogemcraft/forge/biome_modifier/，在 Fabric 下不生效，
 * 这里用 BiomeModifications API 在代码中注册相同的特征注入。
 */
public final class PGCBiomeModifications {
    private static final TagKey<Biome> OVERWORLD = TagKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", "is_overworld"));
    private static final TagKey<Biome> NETHER = TagKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", "is_nether"));
    private static final TagKey<Biome> FOREST = TagKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", "is_forest"));
    private static final TagKey<Biome> SNOWY = TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "is_snowy"));

    private PGCBiomeModifications() {
    }

    public static void register() {
        var ores = GenerationStep.Feature.UNDERGROUND_ORES;
        var surface = GenerationStep.Feature.SURFACE_STRUCTURES;
        var vegetal = GenerationStep.Feature.VEGETAL_DECORATION;

        // 主世界矿石
        addTag(OVERWORLD, ores, "primogem_ore");
        addTag(OVERWORLD, ores, "deepslate_primogem_ore");
        addTag(OVERWORLD, ores, "elemental_crystal_ore");
        addTag(OVERWORLD, ores, "deepslate_elemental_crystal_ore");
        addTag(OVERWORLD, ores, "otherworld_crystal_ore");
        addTag(OVERWORLD, ores, "deepslate_otherworld_crystal_ore");
        addTag(OVERWORLD, ores, "prithiva_topaz_ore");
        addTag(OVERWORLD, ores, "deepslate_prithiva_topaz_ore");
        addTag(OVERWORLD, ores, "nagadus_emerald_ore");
        addTag(OVERWORLD, ores, "shivada_jade_ore");
        addTag(OVERWORLD, ores, "white_iron_ore_block");
        addTag(OVERWORLD, ores, "deepslate_white_iron_ore_block");

        // 下界矿石
        addTag(NETHER, ores, "agnidus_agate_ore");

        // 雪地矿石
        addTag(SNOWY, ores, "geomarrow_crystal_block");

        // 森林植被
        addTag(FOREST, vegetal, "nagadus_emerald_tree");

        // 特定生物群系矿石
        addBiomes(ores, "vajrada_amethyst_ore", "beach", "river", "lukewarm_ocean");
        addBiomes(ores, "varunada_lazurite_ore", "cold_ocean", "deep_cold_ocean", "deep_frozen_ocean",
                "deep_lukewarm_ocean", "deep_ocean", "plains", "frozen_ocean", "lukewarm_ocean", "warm_ocean", "ocean");
        addBiomes(ores, "vayuda_turquoise_ore", "plains");

        // 主世界地表结构
        addTag(OVERWORLD, surface, "big_jar_cluster");
        addTag(OVERWORLD, surface, "medium_jar_cluster");
        addTag(OVERWORLD, surface, "small_jar_cluster_1");
        addTag(OVERWORLD, surface, "small_jar_cluster_2");
        addTag(OVERWORLD, surface, "crystal_pile");
        addTag(OVERWORLD, surface, "trash_can");
    }

    private static void addTag(TagKey<Biome> tag, GenerationStep.Feature step, String feature) {
        BiomeModifications.addFeature(ctx -> ctx.hasTag(tag), step, featureKey(feature));
    }

    private static void addBiomes(GenerationStep.Feature step, String feature, String... biomes) {
        Predicate<BiomeSelectionContext> selector = ctx -> {
            var key = ctx.getBiomeKey();
            for (var name : biomes) {
                if (key.getValue().equals(Identifier.of("minecraft", name))) return true;
            }
            return false;
        };
        BiomeModifications.addFeature(selector, step, featureKey(feature));
    }

    private static RegistryKey<PlacedFeature> featureKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, name));
    }
}