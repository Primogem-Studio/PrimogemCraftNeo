package net.per.primogemcraft.network;

import net.minecraft.util.Identifier;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.StreamCodecs;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ThunderingPulsePayload() implements CustomPacketPayload {
    public static final Type<ThunderingPulsePayload> TYPE = new Type<>(new Identifier(MOD_ID, "thundering_pulse"), ThunderingPulsePayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, ThunderingPulsePayload> STREAM_CODEC = StreamCodec.unit(new ThunderingPulsePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
