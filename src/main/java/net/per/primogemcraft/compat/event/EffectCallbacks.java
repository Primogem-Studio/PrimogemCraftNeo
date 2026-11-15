package net.per.primogemcraft.compat.event;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.per.primogemcraft.PrimogemCraft;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges the 1.21 {@code MobEffect} callbacks that 1.20.1 does not have.
 *
 * <p>1.21 added {@code onEffectStarted} and {@code onMobHurt} directly on
 * {@link MobEffect}. 1.20.1 only offers {@code applyEffectTick} /
 * {@code applyInstantenousEffect}, so the start callback is dispatched from
 * {@link MobEffectEvent.Added} and the hurt callback from {@link LivingHurtEvent}.
 *
 * <p>Effects opt in by implementing {@link Started} and/or {@link Hurt}.
 */
@Mod.EventBusSubscriber(modid = PrimogemCraft.MOD_ID)
public final class EffectCallbacks {

    /** Implement to receive the 1.21 {@code onEffectStarted} callback. */
    public interface Started {
        void onEffectStarted(LivingEntity entity, int amplifier);
    }

    /** Implement to receive the 1.21 {@code onMobHurt} callback. */
    public interface Hurt {
        void onMobHurt(LivingEntity entity, int amplifier,
                       net.minecraft.world.damagesource.DamageSource source, float amount);
    }

    /** Tracks which effect instances already fired their start callback. */
    private static final Map<UUID, Map<MobEffect, Integer>> STARTED = new ConcurrentHashMap<>();

    private EffectCallbacks() {
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        var instance = event.getEffectInstance();
        if (instance == null) return;
        if (!(instance.getEffect() instanceof Started started)) return;
        var entity = event.getEntity();
        var perEntity = STARTED.computeIfAbsent(entity.getUUID(), k -> new ConcurrentHashMap<>());
        var previous = perEntity.put(instance.getEffect(), instance.getAmplifier());
        // 1.21 fires onEffectStarted when the effect is newly applied or amplified.
        if (previous == null || previous != instance.getAmplifier())
            started.onEffectStarted(entity, instance.getAmplifier());
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        var instance = event.getEffectInstance();
        if (instance == null) return;
        var perEntity = STARTED.get(event.getEntity().getUUID());
        if (perEntity != null) perEntity.remove(instance.getEffect());
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        var instance = event.getEffectInstance();
        if (instance == null) return;
        var perEntity = STARTED.get(event.getEntity().getUUID());
        if (perEntity != null) perEntity.remove(instance.getEffect());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        var entity = event.getEntity();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (instance.getEffect() instanceof Hurt hurt)
                hurt.onMobHurt(entity, instance.getAmplifier(), event.getSource(), event.getAmount());
        }
    }
}
