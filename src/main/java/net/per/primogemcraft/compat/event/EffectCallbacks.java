package net.per.primogemcraft.compat.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EffectCallbacks {

    public interface Started {
        void onEffectStarted(LivingEntity entity, int amplifier);
    }

    public interface Hurt {
        void onMobHurt(LivingEntity entity, int amplifier, DamageSource source, float amount);
    }

    private static final Map<UUID, Map<StatusEffect, Integer>> STARTED = new ConcurrentHashMap<>();

    private EffectCallbacks() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            for (var instance : entity.getStatusEffects()) {
                if (instance.getEffectType() instanceof Hurt hurt) {
                    hurt.onMobHurt(entity, instance.getAmplifier(), source, amount);
                }
            }
            return true;
        });
    }

    public static void onEffectApplied(LivingEntity entity, StatusEffectInstance instance) {
        if (instance == null) return;
        if (!(instance.getEffectType() instanceof Started started)) return;
        var perEntity = STARTED.computeIfAbsent(entity.getUuid(), k -> new ConcurrentHashMap<>());
        var previous = perEntity.put(instance.getEffectType(), instance.getAmplifier());
        if (previous == null || previous != instance.getAmplifier()) {
            started.onEffectStarted(entity, instance.getAmplifier());
        }
    }

    public static void onEffectRemoved(LivingEntity entity, StatusEffectInstance instance) {
        if (instance == null) return;
        var perEntity = STARTED.get(entity.getUuid());
        if (perEntity != null) perEntity.remove(instance.getEffectType());
    }

    public interface Removed {
        void onStatusEffectRemoved(LivingEntity entity, StatusEffectInstance instance);
    }

    private static final java.util.List<Removed> REMOVED_LISTENERS = new java.util.concurrent.CopyOnWriteArrayList<>();

    public static void addRemovedListener(Removed listener) {
        REMOVED_LISTENERS.add(listener);
    }

    public static void onStatusEffectRemoved(LivingEntity entity, StatusEffectInstance instance) {
        if (instance == null) return;
        for (var listener : REMOVED_LISTENERS) listener.onStatusEffectRemoved(entity, instance);
    }
}