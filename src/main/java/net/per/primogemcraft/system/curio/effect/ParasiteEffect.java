package net.per.primogemcraft.system.curio.effect;

import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ParasiteEffect extends CurioEffect {
    private static final Identifier MODIFIER_ID = Identifier.of(MOD_ID, "parasite");
    private static final double DAMAGE_BONUS = 0.5D;

    public ParasiteEffect() {
        super(StatusEffectCategory.BENEFICIAL, -4756225);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(MODIFIER_ID).toString(), DAMAGE_BONUS, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }
}
