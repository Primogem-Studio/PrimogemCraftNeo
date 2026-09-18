package net.per.primogemcraft.item.curio;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.system.curio.effect.AtDeathsDoorEffect;

public class ClubFaithVoucherItem extends CurioItem {
    private static final String COOLDOWN = "curio/club_faith_voucher";
    private static final int COOLDOWN_TICKS = 300;
    private static final int EFFECT_TICKS = 200;
    private static final double FRAGMENT_CHANCE = 0.2D;

    public ClubFaithVoucherItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public boolean discountsHertaShop() {
        return true;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        victim.addEffect(new MobEffectInstance(PGCEffects.AT_DEATHS_DOOR, EFFECT_TICKS, AtDeathsDoorEffect.amplifierFor(context.player()), false, false));
        if (context.chance(FRAGMENT_CHANCE)) CurioLoot.spawn(impact.level(), victim.position(), new ItemStack(PGCItems.COSMIC_FRAGMENT.get()));
    }
}
