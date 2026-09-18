package net.per.primogemcraft.system.element.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ElementalFlightEffect extends MobEffect {
    private static final double FLIGHT = 1.0D;

    public ElementalFlightEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
        addAttributeModifier(NeoForgeMod.CREATIVE_FLIGHT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/flight"), FLIGHT, AttributeModifier.Operation.ADD_VALUE);
    }
}
