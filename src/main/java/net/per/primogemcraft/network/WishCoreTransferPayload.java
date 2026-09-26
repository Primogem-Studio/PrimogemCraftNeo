package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishCoreTransferPayload(int containerId) implements CustomPacketPayload {
    public static final Type<WishCoreTransferPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wish_core_transfer"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WishCoreTransferPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WishCoreTransferPayload::containerId, WishCoreTransferPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
