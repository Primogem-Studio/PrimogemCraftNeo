package net.per.primogemcraft.network;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record AnemoEffectPayload() implements CustomPacketPayload {
    public static final Type<AnemoEffectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "anemo_effect"), AnemoEffectPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, AnemoEffectPayload> STREAM_CODEC = StreamCodec.unit(new AnemoEffectPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
