package net.per.primogemcraft.compat.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 1.21 NeoForge 网络注册体系到 1.20.1 Forge {@code SimpleChannel} 的桥接。
 *
 * <p>1.21 通过 {@code RegisterPayloadHandlersEvent} + {@code PayloadRegistrar}
 * 注册载荷，载荷自带 {@code StreamCodec}；1.20.1 用
 * {@code SimpleChannel.registerMessage(index, Class, encode, decode, handler)}。
 * 本类持有唯一的 {@code SimpleChannel}，把 1.21 形态的注册调用翻译成 Forge 形态，
 * 使 {@code PGCNetwork} 的注册代码几乎无需改写。
 *
 * <p>通道在类初始化时创建（即模组构造阶段），早于 Forge 的网络注册锁定。
 */
public final class PayloadRegistrar {

    private static final ResourceLocation CHANNEL_ID =
            new ResourceLocation(net.per.primogemcraft.PrimogemCraft.MOD_ID, "payloads");

    private static final String PROTOCOL_VERSION = "1.1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_ID,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextIndex = 0;

    /** 由 {@link RegisterPayloadHandlersEvent#registrar(String)} 创建。 */
    PayloadRegistrar() {
    }

    public static SimpleChannel channel() {
        return CHANNEL;
    }

    public static String protocolVersion() {
        return PROTOCOL_VERSION;
    }



    /** 对应 1.21 的 {@code registrar.playToServer(...)}。 */
    public <T extends CustomPacketPayload> void playToServer(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            PlayPayloadHandler<T> handler) {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientHandlerBridge.hold(type.payloadClass(), handler);
        register(type, codec, handler, NetworkDirection.PLAY_TO_SERVER);
    }

    /** 对应 1.21 的 {@code registrar.playToClient(...)}。 */
    public <T extends CustomPacketPayload> void playToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            PlayPayloadHandler<T> handler) {
        if (FMLEnvironment.dist == Dist.CLIENT) ClientHandlerBridge.hold(type.payloadClass(), handler);
        register(type, codec, handler, NetworkDirection.PLAY_TO_CLIENT);
    }

    @SuppressWarnings("unchecked")
    private <T extends CustomPacketPayload> void register(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            PlayPayloadHandler<T> handler,
            NetworkDirection direction) {
        var resolved = (PlayPayloadHandler<T>) ClientHandlerBridge.resolve(type.payloadClass(), handler);
        CHANNEL.registerMessage(
                nextIndex++,
                type.payloadClass(),
                (payload, buffer) -> codec.encode(RegistryFriendlyByteBuf.wrap(buffer), payload),
                buffer -> codec.decode(RegistryFriendlyByteBuf.wrap(buffer)),
                (payload, contextSupplier) -> {
                    var forgeContext = contextSupplier.get();
                    forgeContext.enqueueWork(() -> resolved.accept(payload, new Context(forgeContext)));
                    forgeContext.setPacketHandled(true);
                },
                Optional.of(direction));
    }



    /** 对应 1.21 的 {@code PacketDistributor.sendToServer(payload)}。 */
    public static void sendToServer(CustomPacketPayload... payloads) {
        for (CustomPacketPayload payload : payloads) CHANNEL.sendToServer(payload);
    }

    /** 对应 1.21 的 {@code PacketDistributor.sendToPlayer(player, payload...)}。 */
    public static void sendToPlayer(Player player, CustomPacketPayload... payloads) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        var target = PacketDistributor.PLAYER.with(() -> serverPlayer);
        for (CustomPacketPayload payload : payloads) CHANNEL.send(target, payload);
    }

    /** 对应 1.21 的 {@code PacketDistributor.sendToAllPlayers(payload...)}。 */
    public static void sendToAllPlayers(CustomPacketPayload... payloads) {
        var target = PacketDistributor.ALL.noArg();
        for (CustomPacketPayload payload : payloads) CHANNEL.send(target, payload);
    }

    /** 对应 1.21 的 {@code PacketDistributor.sendToPlayersNear(level, excluded, x, y, z, radius, payload)}。 */
    public static void sendToPlayersNear(Level level, ServerPlayer excluded,
                                         double x, double y, double z, double radius,
                                         CustomPacketPayload... payloads) {
        if (level == null) return;
        var target = PacketDistributor.NEAR.with(
                () -> new PacketDistributor.TargetPoint(excluded, x, y, z, radius, level.dimension()));
        for (CustomPacketPayload payload : payloads) CHANNEL.send(target, payload);
    }

    public static void sendToPlayersTrackingEntity(net.minecraft.world.entity.Entity entity, CustomPacketPayload... payloads) {
        var tracking = PacketDistributor.TRACKING_ENTITY.with(() -> entity);
        for (CustomPacketPayload payload : payloads) CHANNEL.send(tracking, payload);
    }

    /**
     * 对应 1.21 的 {@code IPayloadContext}：处理载荷时可取到玩家。
     *
     * <p>服务端方向用 {@code getSender()}；客户端方向取本地玩家。
     * {@code Minecraft} 仅在客户端分支的隔离内部类中触碰，避免服务端类加载问题。
     */
    public static final class Context {
        private final NetworkEvent.Context forgeContext;

        Context(NetworkEvent.Context forgeContext) {
            this.forgeContext = forgeContext;
        }

        public Player player() {
            if (forgeContext.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                return forgeContext.getSender();
            }
            return ClientHolder.localPlayer();
        }

        public NetworkEvent.Context forge() {
            return forgeContext;
        }
    }

    /** 隔离客户端类引用，服务端不会加载本类。 */
    private static final class ClientHolder {
        static Player localPlayer() {
            return Minecraft.getInstance().player;
        }
    }

    /**
     * 把注册时立即捕获客户端类的处理器换成惰性占位，使专用服务端只解析编解码器。
     */
    private static final class ClientHandlerBridge {
        private static final Map<Class<?>, PlayPayloadHandler<?>> HANDLERS = new HashMap<>();

        static <T extends CustomPacketPayload> void hold(Class<T> type, PlayPayloadHandler<T> handler) {
            HANDLERS.put(type, handler);
        }

        static <T extends CustomPacketPayload> PlayPayloadHandler<?> resolve(
                Class<T> type, PlayPayloadHandler<T> fallback) {
            if (FMLEnvironment.dist != Dist.CLIENT) return (payload, context) -> {
            };
            var held = HANDLERS.remove(type);
            return held != null ? held : fallback;
        }
    }

    /** 载荷处理回调，对应 1.21 的 {@code PlayPayloadHandler<T>}。 */
    @FunctionalInterface
    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void accept(T payload, Context context);
    }

    /** 供需要直接编码载荷的场合使用。 */
    public static <T extends CustomPacketPayload> void encode(StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                               FriendlyByteBuf buffer, T payload) {
        codec.encode(RegistryFriendlyByteBuf.wrap(buffer), payload);
    }
}
