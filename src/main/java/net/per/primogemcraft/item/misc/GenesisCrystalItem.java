package net.per.primogemcraft.item.misc;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GenesisCrystalItem extends EffectFoodItem {
    private static final int COOLDOWN_TICKS = 1600;

    public GenesisCrystalItem(Settings properties) {
        super(properties, null,
                net.per.primogemcraft.util.EffectSpecs.of(StatusEffects.HEALTH_BOOST, 6000, 1),
                net.per.primogemcraft.util.EffectSpecs.of(StatusEffects.FIRE_RESISTANCE, 3200, 1),
                net.per.primogemcraft.util.EffectSpecs.of(StatusEffects.ABSORPTION, 1200, 2));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (entity instanceof ServerPlayerEntity player) player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        return result;
    }
}
