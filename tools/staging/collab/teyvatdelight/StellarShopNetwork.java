package net.per.primogemcraft.collab.teyvatdelight;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.minecraft.text.Text;
import net.per.primogemcraft.compat.network.ComponentSerialization;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.compat.network.RegisterPayloadHandlersEvent;

import java.util.List;
import java.util.function.Consumer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class StellarShopNetwork {
    public static final int TASK_FIRST_SLOT = 32;
    public static final int ENCHANT_FIRST_SLOT = 16;
    private static Consumer<Snapshot> receiver = snapshot -> {};

    private StellarShopNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("9");
        registrar.playToServer(Action.TYPE, Action.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayerEntity player) StellarShop.handle(player, payload);
        });
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, (payload, context) -> receiver.accept(payload));
    }

    /** Installs the client snapshot receiver without loading client classes on a server. */
    public static void setReceiver(Consumer<Snapshot> handler) {
        receiver = handler;
    }

    public record Action(int containerId, int slot, long shopCycle, long day) implements CustomPacketPayload {
        public static final Type<Action> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "stellar_shop_action"), Action.class);
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Action::containerId, ByteBufCodecs.VAR_INT, Action::slot,
                ByteBufCodecs.VAR_LONG, Action::shopCycle, ByteBufCodecs.VAR_LONG, Action::day, Action::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Offer(Text title, int price, int remaining, boolean available) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Offer> CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, Offer::title, ByteBufCodecs.VAR_INT, Offer::price,
                ByteBufCodecs.VAR_INT, Offer::remaining, ByteBufCodecs.BOOL, Offer::available, Offer::new);
    }

    public record Task(List<ItemStack> ingredients, ItemStack reward, Text title, boolean completed, boolean available, long token) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Task> CODEC = StreamCodec.composite(
                net.per.primogemcraft.compat.network.StreamCodecs.list(net.per.primogemcraft.compat.network.StreamCodecs.OPTIONAL_ITEM_STACK), Task::ingredients, net.per.primogemcraft.compat.network.StreamCodecs.OPTIONAL_ITEM_STACK, Task::reward,
                ComponentSerialization.STREAM_CODEC, Task::title, ByteBufCodecs.BOOL, Task::completed,
                ByteBufCodecs.BOOL, Task::available, ByteBufCodecs.VAR_LONG, Task::token, Task::new);
    }

    public record Exchange(int cost, int reward, int remaining, boolean available) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Exchange> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Exchange::cost, ByteBufCodecs.VAR_INT, Exchange::reward,
                ByteBufCodecs.VAR_INT, Exchange::remaining, ByteBufCodecs.BOOL, Exchange::available, Exchange::new);
    }

    public record Details(int curioSeconds, int eventSeconds, long exchangeRevision, List<Exchange> exchanges, List<Task> tasks) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Details> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Details::curioSeconds, ByteBufCodecs.VAR_INT, Details::eventSeconds,
                ByteBufCodecs.VAR_LONG, Details::exchangeRevision, net.per.primogemcraft.compat.network.StreamCodecs.list(Exchange.CODEC), Details::exchanges,
                net.per.primogemcraft.compat.network.StreamCodecs.list(Task.CODEC), Details::tasks, Details::new);
    }

    public record Snapshot(int containerId, long shopCycle, long day, int balance, List<Offer> offers, Details details) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "stellar_shop_snapshot"), Snapshot.class);
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Snapshot::containerId, ByteBufCodecs.VAR_LONG, Snapshot::shopCycle,
                ByteBufCodecs.VAR_LONG, Snapshot::day, ByteBufCodecs.VAR_INT, Snapshot::balance,
                net.per.primogemcraft.compat.network.StreamCodecs.list(Offer.CODEC), Snapshot::offers, Details.CODEC, Snapshot::details, Snapshot::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
