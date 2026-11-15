package net.per.primogemcraft.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

/**
 * 效果实例构造辅助。
 *
 * <p>1.21 的 {@code StatusEffects.*} 是 {@code Holder<MobEffect>}，1.20.1 是
 * {@code MobEffect}，故这里直接接收 MobEffect。
 */
public final class EffectSpecs {
    private EffectSpecs() {
    }

    public static StatusEffectInstance of(StatusEffect effect, int ticks, int amplifier) {
        return new StatusEffectInstance(effect, ticks, amplifier);
    }

    public static StatusEffectInstance hidden(StatusEffect effect, int ticks, int amplifier) {
        return new StatusEffectInstance(effect, ticks, amplifier, false, false);
    }
}