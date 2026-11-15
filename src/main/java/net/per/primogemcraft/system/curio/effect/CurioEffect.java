package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class CurioEffect extends StatusEffect {
    public CurioEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public void finished(ServerPlayerEntity player, int amplifier) {
    }

    public void cleared(ServerPlayerEntity player, int amplifier) {
    }
}
