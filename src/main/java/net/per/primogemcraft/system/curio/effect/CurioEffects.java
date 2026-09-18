package net.per.primogemcraft.system.curio.effect;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.function.Consumer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class CurioEffects {
    private static final Map<UUID, Map<Holder<MobEffect>, Held>> HELD = new HashMap<>();
    private static final Map<UUID, Set<Holder<MobEffect>>> PROVIDED = new HashMap<>();
    private static final Set<ServerPlayer> RESTORING = new LinkedHashSet<>();
    private static final Set<UUID> SILENT = new LinkedHashSet<>();
    private static final List<Deferred> DEFERRED = new ArrayList<>();

    private CurioEffects() {
    }

    public static void apply(ServerPlayer player, Holder<MobEffect> effect, int ticks, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, ticks, amplifier, false, true));
    }

    public static void ensure(ServerPlayer player, Holder<MobEffect> effect, int ticks, int amplifier) {
        provided(player).add(effect);
        if (player.hasEffect(effect)) return;
        apply(player, effect, ticks, amplifier);
    }

    public static void replace(ServerPlayer player, Holder<MobEffect> effect, int ticks, int amplifier) {
        silent(player, () -> player.removeEffect(effect));
        apply(player, effect, ticks, amplifier);
    }

    public static void hold(ServerPlayer player, Holder<MobEffect> effect, int ticks, int amplifier) {
        provided(player).add(effect);
        var held = HELD.computeIfAbsent(player.getUUID(), key -> new LinkedHashMap<>());
        var current = held.get(effect);
        if (current == null || current.ticks() != ticks || current.amplifier() != amplifier)
            held.put(effect, new Held(ticks, amplifier));
        var instance = player.getEffect(effect);
        if (instance == null) {
            apply(player, effect, ticks, amplifier);
            return;
        }
        if (instance.getDuration() == MobEffectInstance.INFINITE_DURATION) return;
        if (ticks == MobEffectInstance.INFINITE_DURATION) {
            apply(player, effect, ticks, amplifier);
            return;
        }
        if (instance.getDuration() > ticks / 2) return;
        apply(player, effect, ticks, amplifier);
    }

    public static void release(ServerPlayer player, Holder<MobEffect> effect) {
        forget(player, effect);
        player.removeEffect(effect);
    }

    public static void discard(ServerPlayer player, Holder<MobEffect> effect) {
        forget(player, effect);
        silent(player, () -> player.removeEffect(effect));
    }

    public static void defer(ServerPlayer player, int ticks, Consumer<ServerPlayer> action) {
        DEFERRED.add(new Deferred(player, ticks, action));
    }

    public static boolean active(ServerPlayer player, Holder<MobEffect> effect) {
        return player.hasEffect(effect);
    }

    public static int amplifier(ServerPlayer player, Holder<MobEffect> effect) {
        var instance = player.getEffect(effect);
        return instance == null ? -1 : instance.getAmplifier();
    }

    public static int remaining(ServerPlayer player, Holder<MobEffect> effect) {
        var instance = player.getEffect(effect);
        return instance == null ? 0 : instance.getDuration();
    }

    public static void clear(ServerPlayer player) {
        var provided = PROVIDED.remove(player.getUUID());
        if (provided == null) return;
        for (var effect : provided) discard(player, effect);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (SILENT.contains(player.getUUID())) return;
        if (isHeld(player, event.getEffect())) {
            event.setCanceled(true);
            return;
        }
        var instance = event.getEffectInstance();
        if (instance == null || !(instance.getEffect().value() instanceof CurioEffect curio)) return;
        curio.cleared(player, instance.getAmplifier());
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var instance = event.getEffectInstance();
        assert instance != null;
        if (isHeld(player, instance.getEffect())) {
            RESTORING.add(player);
            return;
        }
        if (!(instance.getEffect().value() instanceof CurioEffect curio)) return;
        curio.finished(player, instance.getAmplifier());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        restore();
        tickDeferred();
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player)) return;
        clear(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        RESTORING.remove((ServerPlayer) event.getEntity());
        DEFERRED.removeIf(deferred -> deferred.player() == event.getEntity());
    }

    private static void restore() {
        if (RESTORING.isEmpty()) return;
        var players = Set.copyOf(RESTORING);
        RESTORING.clear();
        for (var player : players) {
            var held = HELD.get(player.getUUID());
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

    private static void forget(ServerPlayer player, Holder<MobEffect> effect) {
        var held = HELD.get(player.getUUID());
        if (held != null) held.remove(effect);
        var provided = PROVIDED.get(player.getUUID());
        if (provided != null) provided.remove(effect);
    }

    private static Set<Holder<MobEffect>> provided(ServerPlayer player) {
        return PROVIDED.computeIfAbsent(player.getUUID(), key -> new LinkedHashSet<>());
    }

    private static void silent(ServerPlayer player, Runnable action) {
        SILENT.add(player.getUUID());
        try {
            action.run();
        } finally {
            SILENT.remove(player.getUUID());
        }
    }

    private static boolean isHeld(ServerPlayer player, Holder<MobEffect> effect) {
        var held = HELD.get(player.getUUID());
        return held != null && held.containsKey(effect);
    }

    private record Held(int ticks, int amplifier) {
    }

    private record Deferred(ServerPlayer player, int remaining, Consumer<ServerPlayer> action) {
        private Deferred next() {
            return new Deferred(player, remaining - 1, action);
        }
    }
}
