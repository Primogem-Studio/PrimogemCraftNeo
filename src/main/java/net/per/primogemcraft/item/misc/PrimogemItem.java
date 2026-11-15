package net.per.primogemcraft.item.misc;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.item.tool.DescribedItem;

public class PrimogemItem extends DescribedItem {
    private static final int USE_DURATION = 40;
    private static final int ABSORPTION_TICKS = 180;
    private static final int ABSORPTION_AMPLIFIER = 5;
    private static final int COOLDOWN_TICKS = 100;

    public PrimogemItem(Settings properties) {
        super(properties);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (!(entity instanceof ServerPlayerEntity player)) return result;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, ABSORPTION_TICKS, ABSORPTION_AMPLIFIER));
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return result;
    }
}
