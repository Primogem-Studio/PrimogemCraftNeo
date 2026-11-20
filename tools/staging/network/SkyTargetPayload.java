package net.per.primogemcraft.network;

import net.minecraft.util.Identifier;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.StreamCodecs;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record SkyTargetPayload(int targetId, int ticks) implements CustomPacketPayload {
    public static final Type<SkyTargetPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(MOD_ID, "sky_target"), SkyTargetPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkyTargetPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SkyTargetPayload::targetId,
            ByteBufCodecs.VAR_INT, SkyTargetPayload::ticks, SkyTargetPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
