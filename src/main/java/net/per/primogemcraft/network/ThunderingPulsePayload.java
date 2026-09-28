package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ThunderingPulsePayload() implements CustomPacketPayload {
    public static final Type<ThunderingPulsePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "thundering_pulse"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ThunderingPulsePayload> STREAM_CODEC = StreamCodec.unit(new ThunderingPulsePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
