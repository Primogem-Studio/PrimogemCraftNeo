package net.per.primogemcraft.system.element.effect;

import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.compat.attribute.FlightCompat;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 元素飞行效果。
 *
 * <p>1.21 通过 {@code NeoForgeMod.CREATIVE_FLIGHT} 属性修饰器授予飞行；
 * 1.20.1 没有该属性，改为在效果生效期间经 {@link FlightCompat} 授予
 * {@code abilities.mayfly}，效果结束时收回。行为与原版属性方案等价。
 */
public class ElementalFlightEffect extends StatusEffect {
    private static final Identifier FLIGHT_SOURCE =
            Identifier.of(MOD_ID, "effect/flight");
    private static final int REFRESH_TICKS = 40;

    public ElementalFlightEffect() {
        super(StatusEffectCategory.NEUTRAL, -1);
    }

    @Override
    public void onApplied(LivingEntity entity, net.minecraft.entity.attribute.AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity instanceof PlayerEntity player) FlightCompat.grant(player, FLIGHT_SOURCE, REFRESH_TICKS);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (entity instanceof PlayerEntity player) FlightCompat.grant(player, FLIGHT_SOURCE, REFRESH_TICKS);
        return;
    }

    @Override
    public void onRemoved(LivingEntity entity, net.minecraft.entity.attribute.AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        if (entity instanceof PlayerEntity player) FlightCompat.revoke(player, FLIGHT_SOURCE);
    }
}
