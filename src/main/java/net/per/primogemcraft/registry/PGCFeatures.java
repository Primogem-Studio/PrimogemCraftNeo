package net.per.primogemcraft.registry;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.Feature;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.system.worldgen.StructureTemplateConfiguration;
import net.per.primogemcraft.system.worldgen.StructureTemplateFeature;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCFeatures {
    public static final DeferredRegister<Feature<?>> REGISTRY = DeferredRegister.create(RegistryKeys.FEATURE, MOD_ID);

    public static final DeferredHolder<Feature<?>, StructureTemplateFeature> STRUCTURE_TEMPLATE = REGISTRY.register("structure_template",
            () -> new StructureTemplateFeature(StructureTemplateConfiguration.CODEC));
}
