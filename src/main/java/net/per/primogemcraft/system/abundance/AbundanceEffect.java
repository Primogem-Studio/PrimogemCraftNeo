package net.per.primogemcraft.system.abundance;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCParticles;

public class AbundanceEffect extends MobEffect {
    private static final double PARTICLE_CHANCE = 0.2D;
    private static final int PARTICLE_COUNT = 5;
    private static final double PARTICLE_JITTER = 0.1D;
    private static final double PARTICLE_SPREAD = 1.0D;
    private static final double PARTICLE_SPEED = 0.25D;

    public AbundanceEffect() {
        super(MobEffectCategory.BENEFICIAL, -16038607);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level && level.getRandom().nextDouble() < PARTICLE_CHANCE)
            level.sendParticles(PGCParticles.MARA.get(), entity.getX() + jitter(entity), entity.getY(), entity.getZ() + jitter(entity),
                    PARTICLE_COUNT, 0.0D, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        if (entity instanceof Player) return true;
        if (entity.hasEffect(PGCEffects.CUCKOO_CLOCK_TRICK)) entity.removeEffect(PGCEffects.ABUNDANCE);
        return true;
    }

    private static double jitter(LivingEntity entity) {
        return Mth.nextDouble(entity.getRandom(), -PARTICLE_JITTER, PARTICLE_JITTER);
    }
}
