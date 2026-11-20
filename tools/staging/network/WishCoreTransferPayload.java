package net.per.primogemcraft.network;

import net.minecraft.util.Identifier;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.StreamCodecs;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishCoreTransferPayload(int containerId) implements CustomPacketPayload {
    public static final Type<WishCoreTransferPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "wish_core_transfer"), WishCoreTransferPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, WishCoreTransferPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WishCoreTransferPayload::containerId, WishCoreTransferPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
