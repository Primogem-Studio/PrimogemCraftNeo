package net.per.primogemcraft.system.element.effect;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.per.primogemcraft.util.PGCTimer;

public class ScorchingEffect extends StatusEffect {
    private static final String TIMER = "scorching";
    private static final int INTERVAL = 40;
    private static final int INVULNERABLE_TICKS = 5;
    private static final double HEALTH_RATIO = 0.01D;
    private static final int PARTICLE_COUNT = 1;
    private static final double PARTICLE_OFFSET = 1.0D;

    public ScorchingEffect() {
        super(StatusEffectCategory.HARMFUL, -21961);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (entity.getWorld() instanceof ServerWorld level)
            level.spawnParticles(ParticleTypes.LAVA, entity.getX(), entity.getY() + PARTICLE_OFFSET, entity.getZ(),
                    PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, 0.0D);
        if (!PGCTimer.isDone(entity, TIMER)) return;
        PGCTimer.set(entity, TIMER, INTERVAL);
        entity.hurtTime = 0;
        entity.damage(entity.getDamageSources().lava(), (float) Math.max(amplifier, entity.getHealth() * (amplifier + 1) * HEALTH_RATIO));
        entity.hurtTime = INVULNERABLE_TICKS;
        return;
    }
}
