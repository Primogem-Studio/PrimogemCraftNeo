package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GenesisCrystalItem extends EffectFoodItem {
    private static final int COOLDOWN_TICKS = 1600;

    public GenesisCrystalItem(Properties properties) {
        super(properties, null,
                net.per.primogemcraft.util.EffectSpecs.of(MobEffects.HEALTH_BOOST, 6000, 1),
                net.per.primogemcraft.util.EffectSpecs.of(MobEffects.FIRE_RESISTANCE, 3200, 1),
                net.per.primogemcraft.util.EffectSpecs.of(MobEffects.ABSORPTION, 1200, 2));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return result;
    }
}
