package net.per.primogemcraft.item.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.system.curio.*;

public class AmbergrisCheeseItem extends CurioItem {
    private static final int REGENERATION_TICKS = 4;
    private static final int REGENERATION_AMPLIFIER = 100;
    private static final double REGENERATION_CHANCE = 0.1D;
    private static final double BREAK_CHANCE = 0.01D;
    private static final float HEAL_RATIO = 0.3F;

    public AmbergrisCheeseItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 1, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.XP_PICKED) return;
        if (!context.chance(REGENERATION_CHANCE)) return;
        context.player().addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGENERATION_TICKS, REGENERATION_AMPLIFIER, false, false));
        if (context.chance(BREAK_CHANCE)) context.damage(1);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof ServerPlayer player) player.heal(player.getMaxHealth() * HEAL_RATIO);
        return result;
    }
}
