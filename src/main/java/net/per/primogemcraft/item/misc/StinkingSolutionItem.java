package net.per.primogemcraft.item.misc;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;

public class StinkingSolutionItem extends EffectFoodItem {
    private static final int USE_DURATION = 30;

    public StinkingSolutionItem(Settings properties, StatusEffectInstance... effects) {
        super(properties, null, effects);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.DRINK;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }
}
