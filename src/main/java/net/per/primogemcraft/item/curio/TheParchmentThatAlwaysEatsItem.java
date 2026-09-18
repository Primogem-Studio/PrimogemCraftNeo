package net.per.primogemcraft.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class TheParchmentThatAlwaysEatsItem extends CurioItem {
    private static final double RADIUS = 1.0D;
    private static final double HEALTH_THRESHOLD = 0.8D;
    private static final int INVULNERABLE_TICKS = 10;

    public TheParchmentThatAlwaysEatsItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        for (var target : impact.level().getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(RADIUS))) {
            if (target == context.player()) continue;
            var health = target.getHealth();
            var threshold = target.getMaxHealth() * HEALTH_THRESHOLD;
            if (health < threshold) continue;
            target.invulnerableTime = 0;
            target.hurt(impact.level().damageSources().genericKill(), (float) (health - threshold));
            target.invulnerableTime = INVULNERABLE_TICKS;
        }
    }
}
