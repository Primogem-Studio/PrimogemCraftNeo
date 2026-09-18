package net.per.primogemcraft.item.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.system.curio.*;

public class WormInfestedCheeseItem extends CurioItem {
    private static final int USE_DURATION = 32;
    private static final int PARASITE_LEVEL = 2;
    private static final int PARASITE_TICKS = 1200;

    public WormInfestedCheeseItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.EMPTY;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return result;
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
