package net.per.primogemcraft.system.curio.effect;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.per.primogemcraft.system.curio.DeathSaveCurioItem;

public class DeathbedEffect extends CurioEffect {
    public DeathbedEffect() {
        super(StatusEffectCategory.BENEFICIAL, -26368);
    }

    @Override
    public void finished(ServerPlayerEntity player, int amplifier) {
        DeathSaveCurioItem.punish(player);
    }

    @Override
    public void cleared(ServerPlayerEntity player, int amplifier) {
        DeathSaveCurioItem.punish(player);
    }
}
