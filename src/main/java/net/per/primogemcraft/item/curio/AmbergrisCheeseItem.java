package net.per.primogemcraft.item.curio;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.system.curio.*;

public class AmbergrisCheeseItem extends CurioItem {
    private static final int REGENERATION_TICKS = 4;
    private static final int REGENERATION_AMPLIFIER = 100;
    private static final double REGENERATION_CHANCE = 0.1D;
    private static final double BREAK_CHANCE = 0.01D;
    private static final float HEAL_RATIO = 0.3F;

    public AmbergrisCheeseItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, 1, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.XP_PICKED) return;
        if (!context.chance(REGENERATION_CHANCE)) return;
        context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, REGENERATION_TICKS, REGENERATION_AMPLIFIER, false, false));
        if (context.chance(BREAK_CHANCE)) context.damage(1);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (entity instanceof ServerPlayerEntity player) player.heal(player.getMaxHealth() * HEAL_RATIO);
        return result;
    }
}
