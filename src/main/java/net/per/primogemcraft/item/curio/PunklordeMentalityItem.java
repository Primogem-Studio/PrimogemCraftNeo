package net.per.primogemcraft.item.curio;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class PunklordeMentalityItem extends CurioItem {
    private static final int DURATION = 100;
    private static final float CHANCE = 0.5F;

    public PunklordeMentalityItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.HURT) return;
        if (!context.chance(CHANCE)) return;
        switch (MathHelper.nextInt(context.random(), 0, 3)) {
            case 0 -> inflict(context.player(), impact.subject(), StatusEffects.WITHER, 0);
            case 1 -> inflict(context.player(), impact.subject(), StatusEffects.SLOWNESS, 2);
            case 2 -> inflict(context.player(), impact.subject(), StatusEffects.BLINDNESS, 2);
            default -> inflict(context.player(), impact.subject(), StatusEffects.DARKNESS, 2);
        }
    }

    private static void inflict(ServerPlayerEntity player, Entity attacker, StatusEffect effect, int amplifier) {
        if (!(attacker instanceof LivingEntity living) || living == player) return;
        living.addStatusEffect(new StatusEffectInstance(effect, DURATION, amplifier, false, false));
    }
}
