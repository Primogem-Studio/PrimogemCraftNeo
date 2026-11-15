package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectCategory;

public class ThalanToxiFlameEffect extends CurioEffect {
    private static final float LOSS = 2.0F;

    public ThalanToxiFlameEffect() {
        super(StatusEffectCategory.HARMFUL, -3355393);
    }

    @Override
    public void finished(ServerPlayerEntity player, int amplifier) {
        player.damage(player.getDamageSources().generic(), LOSS);
    }
}
