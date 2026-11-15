package net.per.primogemcraft.network;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.util.Identifier;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ChoiceResultPayload(int requestId, int index, boolean accepted) implements CustomPacketPayload {
    public static final Type<ChoiceResultPayload> TYPE = new Type<>(Identifier.of(MOD_ID, "choice_result"), ChoiceResultPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceResultPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ChoiceResultPayload::requestId, ByteBufCodecs.VAR_INT, ChoiceResultPayload::index, ByteBufCodecs.BOOL, ChoiceResultPayload::accepted, ChoiceResultPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
