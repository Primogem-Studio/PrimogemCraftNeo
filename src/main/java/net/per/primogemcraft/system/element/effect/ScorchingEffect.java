package net.per.primogemcraft.system.element.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.util.PGCTimer;

public class ScorchingEffect extends MobEffect {
    private static final String TIMER = "scorching";
    private static final int INTERVAL = 40;
    private static final int INVULNERABLE_TICKS = 5;
    private static final double HEALTH_RATIO = 0.01D;
    private static final int PARTICLE_COUNT = 1;
    private static final double PARTICLE_OFFSET = 1.0D;

    public ScorchingEffect() {
        super(MobEffectCategory.HARMFUL, -21961);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level)
            level.sendParticles(ParticleTypes.LAVA, entity.getX(), entity.getY() + PARTICLE_OFFSET, entity.getZ(),
                    PARTICLE_COUNT, 0.0D, 0.0D, 0.0D, 0.0D);
        if (!PGCTimer.isDone(entity, TIMER)) return true;
        PGCTimer.set(entity, TIMER, INTERVAL);
        entity.invulnerableTime = 0;
        entity.hurt(entity.damageSources().lava(), (float) Math.max(amplifier, entity.getHealth() * (amplifier + 1) * HEALTH_RATIO));
        entity.invulnerableTime = INVULNERABLE_TICKS;
        return true;
    }
}
