package net.per.primogemcraft.system.tool.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class AttackBoostEffect extends MobEffect {
    private static final double ATTACK_BOOST_PER_LEVEL = 0.01D;

    public AttackBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, -1);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "attack_boost"), ATTACK_BOOST_PER_LEVEL,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
