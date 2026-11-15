package net.per.primogemcraft.item.curio;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.per.primogemcraft.system.curio.*;

public class WormInfestedCheeseItem extends CurioItem {
    private static final int USE_DURATION = 32;
    private static final int PARASITE_LEVEL = 2;
    private static final int PARASITE_TICKS = 1200;

    public WormInfestedCheeseItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return net.minecraft.util.UseAction.EAT;
    }

    @Override
    public SoundEvent getEatSound() {
        return SoundEvents.INTENTIONALLY_EMPTY;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (!(entity instanceof ServerPlayerEntity player)) return result;
        Parasitism.clear(player);
        Parasitism.apply(player, PARASITE_LEVEL, PARASITE_TICKS);
        return result;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.XP_PICKED) return;
        Parasitism.clear(context.player());
    }
}
