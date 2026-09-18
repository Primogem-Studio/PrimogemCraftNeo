package net.per.primogemcraft.util;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class EffectSpecs {
    private EffectSpecs() {
    }

    public static MobEffectInstance of(Holder<MobEffect> effect, int ticks, int amplifier) {
        return new MobEffectInstance(effect, ticks, amplifier);
    }

    public static MobEffectInstance hidden(Holder<MobEffect> effect, int ticks, int amplifier) {
        return new MobEffectInstance(effect, ticks, amplifier, false, false);
    }
}
