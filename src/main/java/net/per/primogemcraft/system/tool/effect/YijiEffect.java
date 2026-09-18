package net.per.primogemcraft.system.tool.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.registry.PGCBlocks;
import org.joml.Vector3f;

public class YijiEffect extends MobEffect {
    private static final int SLOWNESS_TICKS = 300;
    private static final int SLOWNESS_AMPLIFIER = 127;
    private static final int DETONATION_TICK = 160;
    private static final int SECOND_STRIKE_DELAY = 20;
    private static final float EXPLOSION_POWER = 8.0F;
    private static final float MAGIC_DAMAGE = 20.0F;
    private static final double BURIAL_CHANCE = 0.5D;
    private static final double VOID_Y = -63.0D;
    private static final double PARTICLE_SPREAD = 2.0D;
    private static final double PARTICLE_LOW = 0.0D;
    private static final double PARTICLE_HIGH = 3.0D;
    private static final double PARTICLE_HIGH_LOW = 1.0D;
    private static final double PARTICLE_HIGH_TOP = 4.0D;
    private static final int CHANNEL_MAX = 255;
    private static final float SOUND_VOLUME = 10.0F;
    private static final float SOUND_PITCH = 0.5F;

    private static final DustParticleOptions BRIGHT = dust(200, 3.0F);
    private static final DustParticleOptions DIM = dust(100, 1.0F);
    private static final DustParticleOptions MID = dust(150, 2.0F);

    public YijiEffect() {
        super(MobEffectCategory.NEUTRAL, -8123894);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER, false, false));
        entity.setData(PGCAttachments.YIJI_TIMER.get(), 0);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level)) return true;
        emitParticles(level, entity);
        var elapsed = entity.getData(PGCAttachments.YIJI_TIMER.get()) + 1;
        entity.setData(PGCAttachments.YIJI_TIMER.get(), elapsed);
        if (elapsed == DETONATION_TICK) detonate(level, entity);
        if (elapsed == DETONATION_TICK + SECOND_STRIKE_DELAY && entity instanceof ServerPlayer player && player.isAlive())
            player.hurt(level.damageSources().magic(), MAGIC_DAMAGE);
        return true;
    }

    private static void detonate(ServerLevel level, LivingEntity entity) {
        var position = entity.blockPosition();
        level.explode(null, entity.getX(), entity.getY(), entity.getZ(), EXPLOSION_POWER, Level.ExplosionInteraction.TNT);
        level.playSound(null, position, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        level.setBlockAndUpdate(position, PGCBlocks.PRIMOGEM_ORE.get().defaultBlockState());
        if (level.getRandom().nextDouble() < BURIAL_CHANCE)
            level.setBlockAndUpdate(position.above(), PGCBlocks.PRIMOGEM_ORE.get().defaultBlockState());
        entity.hurt(level.damageSources().magic(), MAGIC_DAMAGE);
        if (entity instanceof Player) return;
        entity.teleportTo(entity.getX(), VOID_Y, entity.getZ());
    }

    private static void emitParticles(ServerLevel level, LivingEntity entity) {
        for (var particle : new DustParticleOptions[]{BRIGHT, DIM, MID}) {
            emit(level, entity, particle, PARTICLE_LOW, PARTICLE_HIGH);
            emit(level, entity, particle, PARTICLE_HIGH_LOW, PARTICLE_HIGH_TOP);
        }
    }

    private static void emit(ServerLevel level, LivingEntity entity, DustParticleOptions particle, double bottom, double top) {
        var random = level.getRandom();
        level.sendParticles(particle,
                entity.getX() + Mth.nextDouble(random, -PARTICLE_SPREAD, PARTICLE_SPREAD),
                entity.getY() + Mth.nextDouble(random, bottom, top),
                entity.getZ() + Mth.nextDouble(random, -PARTICLE_SPREAD, PARTICLE_SPREAD),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static DustParticleOptions dust(int channel, float scale) {
        return new DustParticleOptions(new Vector3f(channel / (float) CHANNEL_MAX, 0.0F, 0.0F), scale);
    }
}
