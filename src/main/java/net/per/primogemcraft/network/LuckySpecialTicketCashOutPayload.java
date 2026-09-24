package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record LuckySpecialTicketCashOutPayload() implements CustomPacketPayload {
    public static final Type<LuckySpecialTicketCashOutPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "lucky_special_ticket_cash_out"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LuckySpecialTicketCashOutPayload> STREAM_CODEC = StreamCodec.unit(new LuckySpecialTicketCashOutPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
