package net.per.primogemcraft.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 鏁堟灉瀹炰緥鏋勯€犺緟鍔┿€? *
 * <p>1.21 鐨?{@code MobEffects.*} 鏄?{@code MobEffect}锛?.20.1 鏄８
 * {@code MobEffect}锛屾晠杩欓噷鐩存帴鎺ユ敹 MobEffect銆? */
public final class EffectSpecs {
    private EffectSpecs() {
    }

    public static MobEffectInstance of(MobEffect effect, int ticks, int amplifier) {
        return new MobEffectInstance(effect, ticks, amplifier);
    }

    public static MobEffectInstance hidden(MobEffect effect, int ticks, int amplifier) {
        return new MobEffectInstance(effect, ticks, amplifier, false, false);
    }
}