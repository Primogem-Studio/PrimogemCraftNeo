package net.per.primogemcraft.system.tool.effect;

import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AttackBoostEffect extends StatusEffect {
    private static final double ATTACK_BOOST_PER_LEVEL = 0.01D;

    public AttackBoostEffect() {
        super(StatusEffectCategory.BENEFICIAL, -1);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(Identifier.of(MOD_ID, "attack_boost")).toString(), ATTACK_BOOST_PER_LEVEL,
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
