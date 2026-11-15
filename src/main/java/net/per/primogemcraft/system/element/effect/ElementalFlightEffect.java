package net.per.primogemcraft.system.element.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.per.primogemcraft.compat.attribute.FlightCompat;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 元素飞行效果。
 *
 * <p>1.21 通过 {@code NeoForgeMod.CREATIVE_FLIGHT} 属性修饰器授予飞行；
 * 1.20.1 没有该属性，改为在效果生效期间经 {@link FlightCompat} 授予
 * {@code abilities.mayfly}，效果结束时收回。行为与原版属性方案等价。
 */
public class ElementalFlightEffect extends MobEffect {
    private static final ResourceLocation FLIGHT_SOURCE =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/flight");
    private static final int REFRESH_TICKS = 40;

    public ElementalFlightEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);
        if (entity instanceof Player player) FlightCompat.grant(player, FLIGHT_SOURCE, REFRESH_TICKS);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier)
    {
        if (entity instanceof Player player) FlightCompat.grant(player, FLIGHT_SOURCE, REFRESH_TICKS);
        return;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (entity instanceof Player player) FlightCompat.revoke(player, FLIGHT_SOURCE);
    }
}
