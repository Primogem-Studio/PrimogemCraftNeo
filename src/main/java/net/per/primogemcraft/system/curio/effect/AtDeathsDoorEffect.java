package net.per.primogemcraft.system.curio.effect;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AtDeathsDoorEffect extends StatusEffect {
    private static final int COLOR = -10092544;
    private static final double HEALTH_PER_LEVEL = 2.0D;
    private static final double HEALTH_RATIO = 0.1D;
    private static final int MAX_LEVEL = 256;

    public AtDeathsDoorEffect() {
        super(StatusEffectCategory.NEUTRAL, COLOR);
        addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(Identifier.of(MOD_ID, "effect.at_deaths_door")).toString(), -HEALTH_PER_LEVEL, EntityAttributeModifier.Operation.ADDITION);
    }

    public static int amplifierFor(LivingEntity source) {
        return MathHelper.clamp((int) (source.getMaxHealth() * HEALTH_RATIO), 1, MAX_LEVEL) - 1;
    }
}
