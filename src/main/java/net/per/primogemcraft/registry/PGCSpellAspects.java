package net.per.primogemcraft.registry;

import net.hackermdch.genshincraft.spell.AspectDefinition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class PGCSpellAspects {
    public static final DeferredRegister<AspectDefinition> REGISTRY = DeferredRegister.create(AspectDefinition.REGISTRY, MOD_ID);
    public static final DeferredHolder<AspectDefinition, AspectDefinition> LIVING_ITEM =
            REGISTRY.register("living_item", () -> new AspectDefinition());

    private PGCSpellAspects() {
    }
}
