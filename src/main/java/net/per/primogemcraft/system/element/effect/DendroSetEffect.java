package net.per.primogemcraft.system.element.effect;

import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DendroSetEffect extends StatusEffect {
    private static final double MAX_HEALTH = 0.1D;

    public DendroSetEffect() {
        super(StatusEffectCategory.NEUTRAL, -1);
        addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(Identifier.of(MOD_ID, "effect/dendro_set")).toString(), MAX_HEALTH, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }
}
