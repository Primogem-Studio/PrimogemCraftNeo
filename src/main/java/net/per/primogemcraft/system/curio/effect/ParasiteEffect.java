package net.per.primogemcraft.system.curio.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ParasiteEffect extends CurioEffect {
    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "parasite");
    private static final double DAMAGE_BONUS = 0.5D;

    public ParasiteEffect() {
        super(MobEffectCategory.BENEFICIAL, -4756225);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, MODIFIER_ID, DAMAGE_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
