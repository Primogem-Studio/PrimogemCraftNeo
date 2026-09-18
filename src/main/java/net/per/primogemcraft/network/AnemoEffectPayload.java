package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record AnemoEffectPayload() implements CustomPacketPayload {
    public static final Type<AnemoEffectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "anemo_effect"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AnemoEffectPayload> STREAM_CODEC = StreamCodec.unit(new AnemoEffectPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
