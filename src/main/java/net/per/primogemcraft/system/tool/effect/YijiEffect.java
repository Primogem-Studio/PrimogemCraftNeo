package net.per.primogemcraft.system.tool.effect;

import net.per.primogemcraft.compat.attachment.Attachments;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.registry.PGCBlocks;
import org.joml.Vector3f;

public class YijiEffect extends StatusEffect implements net.per.primogemcraft.compat.event.EffectCallbacks.Started {
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

    private static final DustParticleEffect BRIGHT = dust(200, 3.0F);
    private static final DustParticleEffect DIM = dust(100, 1.0F);
    private static final DustParticleEffect MID = dust(150, 2.0F);

    public YijiEffect() {
        super(StatusEffectCategory.NEUTRAL, -8123894);
    }

    public void onEffectStarted(LivingEntity entity, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER, false, false));
        Attachments.set(entity, PGCAttachments.YIJI_TIMER.get(), 0);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (!(entity.getWorld() instanceof ServerWorld level)) return;
        emitParticles(level, entity);
        var elapsed = Attachments.get(entity, PGCAttachments.YIJI_TIMER.get()) + 1;
        Attachments.set(entity, PGCAttachments.YIJI_TIMER.get(), elapsed);
        if (elapsed == DETONATION_TICK) detonate(level, entity);
        if (elapsed == DETONATION_TICK + SECOND_STRIKE_DELAY && entity instanceof ServerPlayerEntity player && player.isAlive())
            player.damage(level.getDamageSources().magic(), MAGIC_DAMAGE);
        return;
    }

    private static void detonate(ServerWorld level, LivingEntity entity) {
        var position = entity.getBlockPos();
        level.createExplosion(null, entity.getX(), entity.getY(), entity.getZ(), EXPLOSION_POWER, net.minecraft.world.World.ExplosionSourceType.TNT);
        level.playSound(null, position, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        level.setBlockState(position, PGCBlocks.PRIMOGEM_ORE.get().getDefaultState());
        if (level.getRandom().nextDouble() < BURIAL_CHANCE)
            level.setBlockState(position.up(), PGCBlocks.PRIMOGEM_ORE.get().getDefaultState());
        entity.damage(level.getDamageSources().magic(), MAGIC_DAMAGE);
        if (entity instanceof PlayerEntity) return;
        entity.requestTeleport(entity.getX(), VOID_Y, entity.getZ());
    }

    private static void emitParticles(ServerWorld level, LivingEntity entity) {
        for (var particle : new DustParticleEffect[]{BRIGHT, DIM, MID}) {
            emit(level, entity, particle, PARTICLE_LOW, PARTICLE_HIGH);
            emit(level, entity, particle, PARTICLE_HIGH_LOW, PARTICLE_HIGH_TOP);
        }
    }

    private static void emit(ServerWorld level, LivingEntity entity, DustParticleEffect particle, double bottom, double top) {
        var random = level.getRandom();
        level.spawnParticles(particle,
                entity.getX() + MathHelper.nextDouble(random, -PARTICLE_SPREAD, PARTICLE_SPREAD),
                entity.getY() + MathHelper.nextDouble(random, bottom, top),
                entity.getZ() + MathHelper.nextDouble(random, -PARTICLE_SPREAD, PARTICLE_SPREAD),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static DustParticleEffect dust(int channel, float scale) {
        return new DustParticleEffect(new Vector3f(channel / (float) CHANNEL_MAX, 0.0F, 0.0F), scale);
    }
}
