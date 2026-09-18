package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCPotions {
    public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(Registries.POTION, MOD_ID);

    private static final int DURATION_TICKS = 1200;
    private static final int ABUNDANCE_AMPLIFIER = 0;
    private static final int CONFUSION_AMPLIFIER = 2;

    public static final DeferredHolder<Potion, Potion> ABUNDANCE_ELIXIR = REGISTRY.register("abundance_elixir",
            () -> new Potion(
                    new MobEffectInstance(PGCEffects.ABUNDANCE, DURATION_TICKS, ABUNDANCE_AMPLIFIER, false, true),
                    new MobEffectInstance(MobEffects.CONFUSION, DURATION_TICKS, CONFUSION_AMPLIFIER, false, false),
                    new MobEffectInstance(MobEffects.DIG_SLOWDOWN, DURATION_TICKS, 0, false, false),
                    new MobEffectInstance(MobEffects.WEAKNESS, DURATION_TICKS, 0, false, false),
                    new MobEffectInstance(MobEffects.UNLUCK, DURATION_TICKS, 0, false, false)));
}
