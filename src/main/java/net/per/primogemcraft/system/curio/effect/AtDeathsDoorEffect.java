package net.per.primogemcraft.system.curio.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AtDeathsDoorEffect extends MobEffect {
    private static final int COLOR = -10092544;
    private static final double HEALTH_PER_LEVEL = 2.0D;
    private static final double HEALTH_RATIO = 0.1D;
    private static final int MAX_LEVEL = 256;

    public AtDeathsDoorEffect() {
        super(MobEffectCategory.NEUTRAL, COLOR);
        addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect.at_deaths_door"), -HEALTH_PER_LEVEL, AttributeModifier.Operation.ADD_VALUE);
    }

    public static int amplifierFor(LivingEntity source) {
        return Mth.clamp((int) (source.getMaxHealth() * HEALTH_RATIO), 1, MAX_LEVEL) - 1;
    }
}
