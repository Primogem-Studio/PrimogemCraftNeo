package net.per.primogemcraft.compat.attribute;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlightCompat {

    private static final Map<UUID, Map<Identifier, Integer>> ACTIVE = new HashMap<>();

    private FlightCompat() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ACTIVE.isEmpty()) return;
            ACTIVE.entrySet().removeIf(entry -> {
                var player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player == null) return true;
                entry.getValue().entrySet().removeIf(source -> {
                    var remaining = source.getValue() - 1;
                    if (remaining > 0) {
                        source.setValue(remaining);
                        return false;
                    }
                    return true;
                });
                if (entry.getValue().isEmpty()) {
                    disable(player);
                    return true;
                }
                return false;
            });
        });
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!isFlying(oldPlayer)) return;
            var sources = ACTIVE.remove(oldPlayer.getUuid());
            if (sources != null && !sources.isEmpty()) {
                ACTIVE.put(newPlayer.getUuid(), sources);
                enable(newPlayer);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            var sources = ACTIVE.remove(player.getUuid());
            if (sources != null && !sources.isEmpty()) disable(player);
        });
    }

    public static void grant(PlayerEntity player, Identifier source, int ticks) {
        if (player.getWorld().isClient() || ticks <= 0) return;
        ACTIVE.computeIfAbsent(player.getUuid(), key -> new HashMap<>()).put(source, ticks);
        enable(player);
    }

    public static void revoke(PlayerEntity player, Identifier source) {
        var sources = ACTIVE.get(player.getUuid());
        if (sources == null) return;
        sources.remove(source);
        if (sources.isEmpty()) {
            ACTIVE.remove(player.getUuid());
            disable(player);
        }
    }

    public static boolean isFlying(PlayerEntity player) {
        var sources = ACTIVE.get(player.getUuid());
        return sources != null && !sources.isEmpty();
    }

    private static void enable(PlayerEntity player) {
        if (player.getAbilities().allowFlying) return;
        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();
    }

    private static void disable(PlayerEntity player) {
        if (player.isCreative() || player.isSpectator()) return;
        if (!player.getAbilities().allowFlying) return;
        player.getAbilities().allowFlying = false;
        player.getAbilities().flying = false;
        player.sendAbilitiesUpdate();
    }
}