package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.tool.DescribedItem;

public class PrimogemItem extends DescribedItem {
    private static final int USE_DURATION = 40;
    private static final int ABSORPTION_TICKS = 180;
    private static final int ABSORPTION_AMPLIFIER = 5;
    private static final int COOLDOWN_TICKS = 100;

    public PrimogemItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return result;
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_TICKS, ABSORPTION_AMPLIFIER));
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        return result;
    }
}
