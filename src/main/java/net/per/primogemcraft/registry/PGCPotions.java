package net.per.primogemcraft.registry;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCPotions {
    public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(RegistryKeys.POTION, MOD_ID);

    private static final int DURATION_TICKS = 1200;
    private static final int ABUNDANCE_AMPLIFIER = 0;
    private static final int CONFUSION_AMPLIFIER = 2;

    public static final DeferredHolder<Potion, Potion> ABUNDANCE_ELIXIR = REGISTRY.register("abundance_elixir",
            () -> new Potion(
                    new StatusEffectInstance(PGCEffects.ABUNDANCE.get(), DURATION_TICKS, ABUNDANCE_AMPLIFIER, false, true),
                    new StatusEffectInstance(StatusEffects.NAUSEA, DURATION_TICKS, CONFUSION_AMPLIFIER, false, false),
                    new StatusEffectInstance(StatusEffects.MINING_FATIGUE, DURATION_TICKS, 0, false, false),
                    new StatusEffectInstance(StatusEffects.WEAKNESS, DURATION_TICKS, 0, false, false),
                    new StatusEffectInstance(StatusEffects.UNLUCK, DURATION_TICKS, 0, false, false)));
}
