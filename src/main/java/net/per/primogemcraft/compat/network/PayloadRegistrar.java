package net.per.primogemcraft.compat.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PayloadRegistrar {

    private static volatile String protocolVersion = "1.0";

    private static final Map<Identifier, StreamCodec<RegistryFriendlyByteBuf, ?>> CODECS = new ConcurrentHashMap<>();

    PayloadRegistrar() {
    }

    public static String protocolVersion() {
        return protocolVersion;
    }

    static void setProtocolVersion(String version) {
        if (version != null && !version.isBlank()) protocolVersion = version;
    }

    public <T extends CustomPacketPayload> void playToServer(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            PlayPayloadHandler<T> handler) {
        CODECS.put(type.id(), codec);
        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, networkHandler, buf, responseSender) -> {
            var payload = codec.decode(RegistryFriendlyByteBuf.wrap(buf));
            server.execute(() -> handler.accept(payload, new Context(player)));
        });
    }

    public <T extends CustomPacketPayload> void playToClient(
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            PlayPayloadHandler<T> handler) {
        CODECS.put(type.id(), codec);
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, networkHandler, buf, responseSender) -> {
            var payload = codec.decode(RegistryFriendlyByteBuf.wrap(buf));
            client.execute(() -> handler.accept(payload, new Context(ClientHolder.localPlayer())));
        });
    }

    /**
     * 只注册编解码器，不注册收包器。
     *
     * <p>用于 S2C 包在专用服务端的发送侧：服务端需要 codec 才能编码，
     * 但不能注册依赖客户端类的收包器。
     */
    public static <T extends CustomPacketPayload> void registerCodecs(Object... typeAndCodecPairs) {
        for (var i = 0; i + 1 < typeAndCodecPairs.length; i += 2) {
            @SuppressWarnings("unchecked")
            var type = (CustomPacketPayload.Type<T>) typeAndCodecPairs[i];
            @SuppressWarnings("unchecked")
            var codec = (StreamCodec<RegistryFriendlyByteBuf, T>) typeAndCodecPairs[i + 1];
            CODECS.put(type.id(), codec);
        }
    }

    public static void sendToServer(CustomPacketPayload... payloads) {
        for (var payload : payloads) {
            ClientPlayNetworking.send(payload.type().id(), encode(payload));
        }
    }

    public static void sendToPlayer(PlayerEntity player, CustomPacketPayload... payloads) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        for (var payload : payloads) {
            ServerPlayNetworking.send(serverPlayer, payload.type().id(), encode(payload));
        }
    }

    public static void sendToAllPlayers(CustomPacketPayload... payloads) {
        var server = net.per.primogemcraft.PrimogemCraft.server();
        if (server == null) return;
        for (var player : PlayerLookup.all(server)) {
            sendToPlayer(player, payloads);
        }
    }

    public static void sendToPlayersNear(World level, ServerPlayerEntity excluded,
                                         double x, double y, double z, double radius,
                                         CustomPacketPayload... payloads) {
        if (!(level instanceof ServerWorld serverWorld)) return;
        for (var player : PlayerLookup.around(serverWorld, new Vec3d(x, y, z), radius)) {
            if (player == excluded) continue;
            sendToPlayer(player, payloads);
        }
    }

    @SuppressWarnings("unchecked")
    private static PacketByteBuf encode(CustomPacketPayload payload) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        var codec = (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) CODECS.get(payload.type().id());
        if (codec == null) {
            throw new IllegalStateException("No codec registered for payload " + payload.type().id());
        }
        codec.encode(RegistryFriendlyByteBuf.wrap(buf), payload);
        return buf;
    }

    public static final class Context {
        private final PlayerEntity player;

        Context(PlayerEntity player) {
            this.player = player;
        }

        public PlayerEntity player() {
            return player;
        }
    }

    private static final class ClientHolder {
        static PlayerEntity localPlayer() {
            return MinecraftClient.getInstance().player;
        }
    }

    @FunctionalInterface
    public interface PlayPayloadHandler<T extends CustomPacketPayload> {
        void accept(T payload, Context context);
    }

    public static <T extends CustomPacketPayload> void encode(StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                               PacketByteBuf buffer, T payload) {
        codec.encode(RegistryFriendlyByteBuf.wrap(buffer), payload);
    }
}