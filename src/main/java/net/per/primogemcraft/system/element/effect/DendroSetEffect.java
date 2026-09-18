package net.per.primogemcraft.system.element.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DendroSetEffect extends MobEffect {
    private static final double MAX_HEALTH = 0.1D;

    public DendroSetEffect() {
        super(MobEffectCategory.NEUTRAL, -1);
        addAttributeModifier(Attributes.MAX_HEALTH, ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/dendro_set"), MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }
}
