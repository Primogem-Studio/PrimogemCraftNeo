package net.per.primogemcraft.system.choice;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.network.ChoiceOpenPayload;
import net.per.primogemcraft.network.ChoiceResultPayload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class ChoiceRegistry {
    public static final int DEFAULT_DURATION = 600;
    public static final int DEFAULT_SETTLE_TICKS = 12;
    public static final int NO_SETTLE_TICKS = 0;
    public static final int NO_ANSWER = -1;

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static int nextId = 1;

    private ChoiceRegistry() {
    }

    public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual, ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin, List<ChoiceCard> options, ChoiceCallback callback) {
        open(player, title, subtitle, visual, texture, textures, spin, DEFAULT_SETTLE_TICKS, ChoiceMode.SELECT, ChoiceRequest.NO_LEAVE, options, callback);
    }

    public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual, ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin, int settleTicks, List<ChoiceCard> options, ChoiceCallback callback) {
        open(player, title, subtitle, visual, texture, textures, spin, settleTicks, ChoiceMode.SELECT, ChoiceRequest.NO_LEAVE, options, callback);
    }

    public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual, ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin, int settleTicks, ChoiceMode mode, List<ChoiceCard> options, ChoiceCallback callback) {
        open(player, title, subtitle, visual, texture, textures, spin, settleTicks, mode, ChoiceRequest.NO_LEAVE, options, callback);
    }

    public static void open(ServerPlayer player, Component title, Component subtitle, ChoiceVisual visual, ResourceLocation texture, ChoiceCardTextures textures, ChoiceSpin spin, int settleTicks, ChoiceMode mode, int leaveIndex, List<ChoiceCard> options, ChoiceCallback callback) {
        if (options.isEmpty()) return;
        cancel(player.getUUID(), NO_ANSWER);
        var id = nextId++;
        var expireTime = player.level().getGameTime() + DEFAULT_DURATION;
        var cards = List.copyOf(options);
        PENDING.put(player.getUUID(), new Pending(id, mode, cards, callback));
        PacketDistributor.sendToPlayer(player, new ChoiceOpenPayload(new ChoiceRequest(id, title, subtitle, visual, texture, textures, spin, Math.max(settleTicks, NO_SETTLE_TICKS), cards, expireTime, mode, leaveIndex)));
    }

    public static boolean isPending(ServerPlayer player) {
        return PENDING.containsKey(player.getUUID());
    }

    public static void respond(ServerPlayer player, int id, int index) {
        var pending = PENDING.get(player.getUUID());
        if (pending == null || pending.id() != id) return;
        if (index < 0) {
            giveUp(player, pending, id);
            return;
        }
        var selected = index < pending.options().size() ? index : 0;
        var accepted = resolve(player, pending, selected);
        if (pending.mode() != ChoiceMode.CONFIRM) {
            PENDING.remove(player.getUUID(), pending);
            return;
        }
        if (accepted) PENDING.remove(player.getUUID(), pending);
        PacketDistributor.sendToPlayer(player, new ChoiceResultPayload(id, selected, accepted));
    }

    private static void giveUp(ServerPlayer player, Pending pending, int id) {
        PENDING.remove(player.getUUID(), pending);
        if (pending.callback() != null) accept(pending.callback(), NO_ANSWER);
        if (pending.mode() == ChoiceMode.CONFIRM)
            PacketDistributor.sendToPlayer(player, new ChoiceResultPayload(id, NO_ANSWER, true));
    }

    public static void cancel(UUID uuid, int index) {
        var pending = PENDING.remove(uuid);
        if (pending == null || pending.callback() == null) return;
        accept(pending.callback(), index);
    }

    private static boolean resolve(ServerPlayer player, Pending pending, int selected) {
        if (pending.callback() == null) {
            if (pending.mode() == ChoiceMode.REVEAL) {
                for (var card : pending.options()) grant(player, card);
                return true;
            }
            grant(player, pending.options().get(selected));
            return true;
        }
        try {
            return pending.callback().select(selected);
        } catch (RuntimeException exception) {
            PrimogemCraft.LOGGER.error("Choice callback failed", exception);
            return true;
        }
    }

    private static void grant(ServerPlayer player, ChoiceCard card) {
        var prize = card.item();
        if (prize.isEmpty()) return;
        player.getInventory().placeItemBackInInventory(prize.copy());
    }

    private static void accept(ChoiceCallback callback, int index) {
        try {
            callback.select(index);
        } catch (RuntimeException exception) {
            PrimogemCraft.LOGGER.error("Choice callback failed", exception);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancel(event.getEntity().getUUID(), NO_ANSWER);
    }

    private record Pending(int id, ChoiceMode mode, List<ChoiceCard> options, ChoiceCallback callback) {
    }
}
