package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishBowShootPayload() implements CustomPacketPayload {
    public static final Type<WishBowShootPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wish_bow_shoot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WishBowShootPayload> STREAM_CODEC = StreamCodec.unit(new WishBowShootPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
