package net.per.primogemcraft.item.curio;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class PunklordeMentalityItem extends CurioItem {
    private static final int DURATION = 100;
    private static final float CHANCE = 0.5F;

    public PunklordeMentalityItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.HURT) return;
        if (!context.chance(CHANCE)) return;
        switch (Mth.nextInt(context.random(), 0, 3)) {
            case 0 -> inflict(context.player(), impact.subject(), MobEffects.WITHER, 0);
            case 1 -> inflict(context.player(), impact.subject(), MobEffects.MOVEMENT_SLOWDOWN, 2);
            case 2 -> inflict(context.player(), impact.subject(), MobEffects.BLINDNESS, 2);
            default -> inflict(context.player(), impact.subject(), MobEffects.DARKNESS, 2);
        }
    }

    private static void inflict(ServerPlayer player, Entity attacker, Holder<MobEffect> effect, int amplifier) {
        if (!(attacker instanceof LivingEntity living) || living == player) return;
        living.addEffect(new MobEffectInstance(effect, DURATION, amplifier, false, false));
    }
}
