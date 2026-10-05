package net.per.primogemcraft.registry;

import com.mojang.serialization.MapCodec;
import net.hackermdch.genshincraft.spell.PrimitiveType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.collab.genshincraft.UtilityPulsePrimitive;
import net.per.primogemcraft.collab.genshincraft.LivingItemPrimitive;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class PGCSpellPrimitiveTypes {
    public static final DeferredRegister<PrimitiveType<?>> REGISTRY = DeferredRegister.create(PrimitiveType.REGISTRY, MOD_ID);
    public static final DeferredHolder<PrimitiveType<?>, PrimitiveType<UtilityPulsePrimitive>> UTILITY_PULSE =
            REGISTRY.register("utility_pulse", () -> new PrimitiveType<UtilityPulsePrimitive>() {
                @Override
                public MapCodec<UtilityPulsePrimitive> codec() {
                    return UtilityPulsePrimitive.CODEC;
                }
            });

    public static final DeferredHolder<PrimitiveType<?>, PrimitiveType<LivingItemPrimitive>> LIVING_ITEM =
            REGISTRY.register("living_item", () -> new PrimitiveType<LivingItemPrimitive>() {
                @Override
                public MapCodec<LivingItemPrimitive> codec() {
                    return LivingItemPrimitive.CODEC;
                }
            });

    private PGCSpellPrimitiveTypes() {
    }
}
