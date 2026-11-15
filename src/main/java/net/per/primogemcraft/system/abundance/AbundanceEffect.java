package net.per.primogemcraft.system.abundance;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCParticles;

public class AbundanceEffect extends StatusEffect {
    private static final double PARTICLE_CHANCE = 0.2D;
    private static final int PARTICLE_COUNT = 5;
    private static final double PARTICLE_JITTER = 0.1D;
    private static final double PARTICLE_SPREAD = 1.0D;
    private static final double PARTICLE_SPEED = 0.25D;

    public AbundanceEffect() {
        super(StatusEffectCategory.BENEFICIAL, -16038607);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (entity.getWorld() instanceof ServerWorld level && level.getRandom().nextDouble() < PARTICLE_CHANCE)
            level.spawnParticles(PGCParticles.MARA.get(), entity.getX() + jitter(entity), entity.getY(), entity.getZ() + jitter(entity),
                    PARTICLE_COUNT, 0.0D, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        if (entity instanceof PlayerEntity) return;
        if (entity.hasStatusEffect(PGCEffects.CUCKOO_CLOCK_TRICK.get())) entity.removeStatusEffect(PGCEffects.ABUNDANCE.get());
        return;
    }

    private static double jitter(LivingEntity entity) {
        return MathHelper.nextDouble(entity.getRandom(), -PARTICLE_JITTER, PARTICLE_JITTER);
    }
}
