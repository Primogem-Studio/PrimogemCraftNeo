package net.per.primogemcraft.system.element.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class HiddenEffect extends StatusEffect {
    public HiddenEffect() {
        super(StatusEffectCategory.NEUTRAL, -1);
    }
}
