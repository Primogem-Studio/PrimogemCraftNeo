package net.per.primogemcraft.util;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class PGCTimer {
    private static final Map<Entity, Map<String, Integer>> TIMERS = new HashMap<>();

    private PGCTimer() {
    }

    public static void set(Entity entity, String name, int delay) {
        TIMERS.computeIfAbsent(entity, key -> new HashMap<>()).put(name, delay);
    }

    public static boolean isDone(Entity entity, String name) {
        Map<String, Integer> timers = TIMERS.get(entity);
        if (timers == null) return true;
        Integer remaining = timers.get(name);
        return remaining == null || remaining <= 0;
    }

    public static void clear(Entity entity) {
        TIMERS.remove(entity);
    }

    public static void clear(Entity entity, String name) {
        var timers = TIMERS.get(entity);
        if (timers != null) timers.remove(name);
    }

    public static void clearMatching(Entity entity, String prefix) {
        var timers = TIMERS.get(entity);
        if (timers != null) timers.keySet().removeIf(key -> key.startsWith(prefix));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        TIMERS.values().forEach(timers -> timers.entrySet().removeIf(entry -> entry.setValue(entry.getValue() - 1) <= 0));
    }
}
