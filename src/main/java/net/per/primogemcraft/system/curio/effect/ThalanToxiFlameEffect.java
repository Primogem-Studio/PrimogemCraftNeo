package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;

public class ThalanToxiFlameEffect extends CurioEffect {
    private static final float LOSS = 2.0F;

    public ThalanToxiFlameEffect() {
        super(MobEffectCategory.HARMFUL, -3355393);
    }

    @Override
    public void finished(ServerPlayer player, int amplifier) {
        player.hurt(player.damageSources().generic(), LOSS);
    }
}
