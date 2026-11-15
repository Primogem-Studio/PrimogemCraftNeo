package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.system.worldgen.StructureTemplateConfiguration;
import net.per.primogemcraft.system.worldgen.StructureTemplateFeature;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(Registries.FEATURE, MOD_ID);

    public static final DeferredHolder<Feature<?>, StructureTemplateFeature> STRUCTURE_TEMPLATE = REGISTRY.register("structure_template",
            () -> new StructureTemplateFeature(StructureTemplateConfiguration.CODEC));
}
