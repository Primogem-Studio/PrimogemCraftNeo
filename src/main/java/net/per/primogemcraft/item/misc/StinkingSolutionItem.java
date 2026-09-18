package net.per.primogemcraft.item.misc;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class StinkingSolutionItem extends EffectFoodItem {
    private static final int USE_DURATION = 30;

    public StinkingSolutionItem(Properties properties, MobEffectInstance... effects) {
        super(properties, null, effects);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }
}
