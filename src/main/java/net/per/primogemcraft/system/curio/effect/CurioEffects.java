package net.per.primogemcraft.system.curio.effect;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.per.primogemcraft.compat.event.EffectCallbacks;

import java.util.*;
import java.util.function.Consumer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class CurioEffects {
    private static final Map<UUID, Map<StatusEffect, Held>> HELD = new HashMap<>();
    private static final Map<UUID, Set<StatusEffect>> PROVIDED = new HashMap<>();
    private static final Set<ServerPlayerEntity> RESTORING = new LinkedHashSet<>();
    private static final Set<UUID> SILENT = new LinkedHashSet<>();
    private static final List<Deferred> DEFERRED = new ArrayList<>();

    private CurioEffects() {
    }

    public static void apply(ServerPlayerEntity player, StatusEffect effect, int ticks, int amplifier) {
        player.addStatusEffect(new StatusEffectInstance(effect, ticks, amplifier, false, true));
    }

    public static void ensure(ServerPlayerEntity player, StatusEffect effect, int ticks, int amplifier) {
        provided(player).add(effect);
        if (player.hasStatusEffect(effect)) return;
        apply(player, effect, ticks, amplifier);
    }

    public static void replace(ServerPlayerEntity player, StatusEffect effect, int ticks, int amplifier) {
        silent(player, () -> player.removeStatusEffect(effect));
        apply(player, effect, ticks, amplifier);
    }

    public static void hold(ServerPlayerEntity player, StatusEffect effect, int ticks, int amplifier) {
        provided(player).add(effect);
        var held = HELD.computeIfAbsent(player.getUuid(), key -> new LinkedHashMap<>());
        var current = held.get(effect);
        if (current == null || current.ticks() != ticks || current.amplifier() != amplifier)
            held.put(effect, new Held(ticks, amplifier));
        var instance = player.getStatusEffect(effect);
        if (instance == null) {
            apply(player, effect, ticks, amplifier);
            return;
        }
        if (instance.getDuration() == StatusEffectInstance.INFINITE) return;
        if (ticks == StatusEffectInstance.INFINITE) {
            apply(player, effect, ticks, amplifier);
            return;
        }
        if (instance.getDuration() > ticks / 2) return;
        apply(player, effect, ticks, amplifier);
    }

    public static void release(ServerPlayerEntity player, StatusEffect effect) {
        forget(player, effect);
        player.removeStatusEffect(effect);
    }

    public static void discard(ServerPlayerEntity player, StatusEffect effect) {
        forget(player, effect);
        silent(player, () -> player.removeStatusEffect(effect));
    }

    public static void defer(ServerPlayerEntity player, int ticks, Consumer<ServerPlayerEntity> action) {
        DEFERRED.add(new Deferred(player, ticks, action));
    }

    public static boolean active(ServerPlayerEntity player, StatusEffect effect) {
        return player.hasStatusEffect(effect);
    }

    public static int amplifier(ServerPlayerEntity player, StatusEffect effect) {
        var instance = player.getStatusEffect(effect);
        return instance == null ? -1 : instance.getAmplifier();
    }

    public static int remaining(ServerPlayerEntity player, StatusEffect effect) {
        var instance = player.getStatusEffect(effect);
        return instance == null ? 0 : instance.getDuration();
    }

    public static void clear(ServerPlayerEntity player) {
        var provided = PROVIDED.remove(player.getUuid());
        if (provided == null) return;
        for (var effect : provided) discard(player, effect);
    }

    public static void register() {
        EffectCallbacks.addRemovedListener((entity, instance) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;
            if (SILENT.contains(player.getUuid())) return;
            if (isHeld(player, instance.getEffectType())) {
                RESTORING.add(player);
                return;
            }
            if (!(instance.getEffectType() instanceof CurioEffect curio)) return;
            curio.cleared(player, instance.getAmplifier());
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            restore();
            tickDeferred();
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) clear(newPlayer);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            RESTORING.remove(handler.getPlayer());
            DEFERRED.removeIf(deferred -> deferred.player() == handler.getPlayer());
        });
    }

    private static void restore() {
        if (RESTORING.isEmpty()) return;
        var players = Set.copyOf(RESTORING);
        RESTORING.clear();
        for (var player : players) {
            var held = HELD.get(player.getUuid());
            if (held == null) continue;
            for (var entry : Map.copyOf(held).entrySet())
                ensure(player, entry.getKey(), entry.getValue().ticks(), entry.getValue().amplifier());
        }
    }

    private static void tickDeferred() {
        if (DEFERRED.isEmpty()) return;
        var pending = List.copyOf(DEFERRED);
        DEFERRED.clear();
        for (var deferred : pending) {
            if (!deferred.player().isAlive()) continue;
            if (deferred.remaining() > 1) {
                DEFERRED.add(deferred.next());
                continue;
            }
            deferred.action().accept(deferred.player());
        }
    }

    private static void forget(ServerPlayerEntity player, StatusEffect effect) {
        var held = HELD.get(player.getUuid());
        if (held != null) held.remove(effect);
        var provided = PROVIDED.get(player.getUuid());
        if (provided != null) provided.remove(effect);
    }

    private static Set<StatusEffect> provided(ServerPlayerEntity player) {
        return PROVIDED.computeIfAbsent(player.getUuid(), key -> new LinkedHashSet<>());
    }

    private static void silent(ServerPlayerEntity player, Runnable action) {
        SILENT.add(player.getUuid());
        try {
            action.run();
        } finally {
            SILENT.remove(player.getUuid());
        }
    }

    private static boolean isHeld(ServerPlayerEntity player, StatusEffect effect) {
        var held = HELD.get(player.getUuid());
        return held != null && held.containsKey(effect);
    }

    private record Held(int ticks, int amplifier) {
    }

    private record Deferred(ServerPlayerEntity player, int remaining, Consumer<ServerPlayerEntity> action) {
        private Deferred next() {
            return new Deferred(player, remaining - 1, action);
        }
    }
}
