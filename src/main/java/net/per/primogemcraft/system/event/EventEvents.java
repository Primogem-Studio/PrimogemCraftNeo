package net.per.primogemcraft.system.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class EventEvents {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof PlayerEntity) return;
            if (!(entity.getWorld() instanceof ServerWorld level)) return;
            if (!(source.getAttacker() instanceof ServerPlayerEntity killer)) return;
            EventCombat.killed(killer, entity);
            EventRegistry.drop(level, entity.getPos(), killer);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EventCombat.tick(server);
            EventChain.tick(server);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            EventChain.clear(handler.getPlayer().getUuid());
        });
    }
}