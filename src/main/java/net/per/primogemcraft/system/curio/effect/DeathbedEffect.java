package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.per.primogemcraft.system.curio.DeathSaveCurioItem;

public class DeathbedEffect extends CurioEffect {
    public DeathbedEffect() {
        super(MobEffectCategory.BENEFICIAL, -26368);
    }

    @Override
    public void finished(ServerPlayer player, int amplifier) {
        DeathSaveCurioItem.punish(player);
    }

    @Override
    public void cleared(ServerPlayer player, int amplifier) {
        DeathSaveCurioItem.punish(player);
    }
}
