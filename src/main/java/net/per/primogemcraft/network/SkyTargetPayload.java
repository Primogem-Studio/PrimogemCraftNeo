package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record SkyTargetPayload(int targetId, int ticks) implements CustomPacketPayload {
    public static final Type<SkyTargetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sky_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkyTargetPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SkyTargetPayload::targetId,
            ByteBufCodecs.VAR_INT, SkyTargetPayload::ticks, SkyTargetPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
