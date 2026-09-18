package net.per.primogemcraft.system.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.per.primogemcraft.system.choice.ChoiceRegistry;

import java.util.*;

public final class EventChain {
    private static final Map<UUID, Deque<Step>> CHAINS = new HashMap<>();

    private EventChain() {
    }

    public static void events(ServerPlayer player, List<Integer> numbers) {
        start(player, numbers, false);
    }

    public static void groups(ServerPlayer player, List<Integer> numbers) {
        start(player, numbers, true);
    }

    public static void tick(MinecraftServer server) {
        if (CHAINS.isEmpty()) return;
        for (var uuid : List.copyOf(CHAINS.keySet())) {
            var player = server.getPlayerList().getPlayer(uuid);
            if (player == null) {
                CHAINS.remove(uuid);
                continue;
            }
            advance(player);
        }
    }

    public static void clear(UUID uuid) {
        CHAINS.remove(uuid);
    }

    private static void start(ServerPlayer player, List<Integer> numbers, boolean group) {
        if (numbers.isEmpty()) return;
        var chain = CHAINS.computeIfAbsent(player.getUUID(), key -> new ArrayDeque<>());
        for (var number : numbers) chain.addLast(new Step(group, number));
        advance(player);
    }

    private static void advance(ServerPlayer player) {
        var uuid = player.getUUID();
        var chain = CHAINS.get(uuid);
        if (chain == null || ChoiceRegistry.isPending(player)) return;
        var step = chain.pollFirst();
        if (step == null) {
            CHAINS.remove(uuid);
            return;
        }
        step.apply(player);
    }

    private record Step(boolean group, int number) {
        void apply(ServerPlayer player) {
            if (group) EventRegistry.trigger(player, number);
            else EventRegistry.run(player, number);
        }
    }
}
