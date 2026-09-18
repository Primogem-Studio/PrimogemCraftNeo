package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CurioEffect extends MobEffect {
    public CurioEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public void finished(ServerPlayer player, int amplifier) {
    }

    public void cleared(ServerPlayer player, int amplifier) {
    }
}
