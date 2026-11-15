package net.per.primogemcraft.network;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ChoiceSelectPayload(int requestId, int index) implements CustomPacketPayload {
    public static final Type<ChoiceSelectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "choice_select"), ChoiceSelectPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChoiceSelectPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ChoiceSelectPayload::requestId, ByteBufCodecs.VAR_INT, ChoiceSelectPayload::index, ChoiceSelectPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
