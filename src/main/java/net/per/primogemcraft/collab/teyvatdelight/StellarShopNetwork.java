package net.per.primogemcraft.collab.teyvatdelight;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.List;
import java.util.function.Consumer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class StellarShopNetwork {
    public static final int TASK_FIRST_SLOT = 32;
    public static final int ENCHANT_FIRST_SLOT = 16;
    private static Consumer<Snapshot> receiver = snapshot -> {};

    private StellarShopNetwork() {
    }

    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("9");
        registrar.playToServer(Action.TYPE, Action.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StellarShop.handle(player, payload);
        });
        registrar.playToClient(Snapshot.TYPE, Snapshot.CODEC, (payload, context) -> receiver.accept(payload));
    }

    /** Installs the client snapshot receiver without loading client classes on a server. */
    public static void setReceiver(Consumer<Snapshot> handler) {
        receiver = handler;
    }

    public record Action(int containerId, int slot, long shopCycle, long day) implements CustomPacketPayload {
        public static final Type<Action> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "stellar_shop_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Action::containerId, ByteBufCodecs.VAR_INT, Action::slot,
                ByteBufCodecs.VAR_LONG, Action::shopCycle, ByteBufCodecs.VAR_LONG, Action::day, Action::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record Offer(Component title, int price, int remaining, boolean available) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Offer> CODEC = StreamCodec.composite(
                ComponentSerialization.STREAM_CODEC, Offer::title, ByteBufCodecs.VAR_INT, Offer::price,
                ByteBufCodecs.VAR_INT, Offer::remaining, ByteBufCodecs.BOOL, Offer::available, Offer::new);
    }

    public record Task(List<ItemStack> ingredients, ItemStack reward, Component title, boolean completed, boolean available, long token) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Task> CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(3)), Task::ingredients, ItemStack.OPTIONAL_STREAM_CODEC, Task::reward,
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
                ByteBufCodecs.VAR_LONG, Details::exchangeRevision, Exchange.CODEC.apply(ByteBufCodecs.list(4)), Details::exchanges,
                Task.CODEC.apply(ByteBufCodecs.list(StellarTaskPlan.MAX_COUNT)), Details::tasks, Details::new);
    }

    public record Snapshot(int containerId, long shopCycle, long day, int balance, List<Offer> offers, Details details) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "stellar_shop_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Snapshot::containerId, ByteBufCodecs.VAR_LONG, Snapshot::shopCycle,
                ByteBufCodecs.VAR_LONG, Snapshot::day, ByteBufCodecs.VAR_INT, Snapshot::balance,
                Offer.CODEC.apply(ByteBufCodecs.list(9)), Snapshot::offers, Details.CODEC, Snapshot::details, Snapshot::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
