package net.per.primogemcraft.item.curio;

import net.minecraft.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class TheParchmentThatAlwaysEatsItem extends CurioItem {
    private static final double RADIUS = 1.0D;
    private static final double HEALTH_THRESHOLD = 0.8D;
    private static final int INVULNERABLE_TICKS = 10;

    public TheParchmentThatAlwaysEatsItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        for (var target : impact.level().getEntitiesByClass(LivingEntity.class, victim.getBoundingBox().expand(RADIUS), e -> true)) {
            if (target == context.player()) continue;
            var health = target.getHealth();
            var threshold = target.getMaxHealth() * HEALTH_THRESHOLD;
            if (health < threshold) continue;
            target.hurtTime = 0;
            target.damage(impact.level().getDamageSources().genericKill(), (float) (health - threshold));
            target.hurtTime = INVULNERABLE_TICKS;
        }
    }
}
