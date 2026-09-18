package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ChoiceSelectPayload(int requestId, int index) implements CustomPacketPayload {
    public static final Type<ChoiceSelectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "choice_select"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceSelectPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ChoiceSelectPayload::requestId, ByteBufCodecs.VAR_INT, ChoiceSelectPayload::index, ChoiceSelectPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
