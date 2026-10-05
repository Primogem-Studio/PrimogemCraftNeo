package net.per.primogemcraft.registry;

import com.mojang.serialization.MapCodec;
import net.hackermdch.genshincraft.spell.DecoratorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.collab.genshincraft.LivingItemDecorator;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class PGCSpellDecoratorTypes {
    public static final DeferredRegister<DecoratorType<?>> REGISTRY = DeferredRegister.create(DecoratorType.REGISTRY, MOD_ID);
    public static final DeferredHolder<DecoratorType<?>, DecoratorType<LivingItemDecorator>> LIVING_ITEM =
            REGISTRY.register("living_item", () -> new DecoratorType<LivingItemDecorator>() {
                @Override
                public MapCodec<LivingItemDecorator> codec() {
                    return LivingItemDecorator.CODEC;
                }
            });

    private PGCSpellDecoratorTypes() {
    }
}
