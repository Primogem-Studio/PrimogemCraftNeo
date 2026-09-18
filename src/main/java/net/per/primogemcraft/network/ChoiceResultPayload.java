package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ChoiceResultPayload(int requestId, int index, boolean accepted) implements CustomPacketPayload {
    public static final Type<ChoiceResultPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "choice_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceResultPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ChoiceResultPayload::requestId, ByteBufCodecs.VAR_INT, ChoiceResultPayload::index, ByteBufCodecs.BOOL, ChoiceResultPayload::accepted, ChoiceResultPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
