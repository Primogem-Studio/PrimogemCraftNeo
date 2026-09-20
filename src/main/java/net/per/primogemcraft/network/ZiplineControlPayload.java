package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ZiplineControlPayload(int targetId) implements CustomPacketPayload {
    public static final Type<ZiplineControlPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "zipline_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ZiplineControlPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ZiplineControlPayload::targetId, ZiplineControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
