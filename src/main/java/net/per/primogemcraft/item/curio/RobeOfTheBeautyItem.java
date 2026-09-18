package net.per.primogemcraft.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.util.PlayerItems;

public class RobeOfTheBeautyItem extends CurioItem {
    private static final String COOLDOWN = "curio/robe_of_the_beauty";
    private static final int COOLDOWN_TICKS = 40;
    private static final int FRAGMENTS_PER_STEP = 64;
    private static final double HEALTH_RATIO = 0.016D;

    public RobeOfTheBeautyItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        var steps = PlayerItems.count(context.player(), PGCItems.COSMIC_FRAGMENT.get()) / FRAGMENTS_PER_STEP;
        if (steps <= 0) return;
        if (!context.ready(COOLDOWN, COOLDOWN_TICKS)) return;
        victim.hurt(victim.damageSources().magic(), (float) (context.player().getMaxHealth() * HEALTH_RATIO * steps));
    }
}
