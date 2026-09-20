package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishDropsPayload(Map<ResourceLocation, List<ResourceLocation>> drops) implements CustomPacketPayload {
    public static final Type<WishDropsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wish_drops"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WishDropsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list())),
            WishDropsPayload::drops, WishDropsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
