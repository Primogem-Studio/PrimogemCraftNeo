package net.per.primogemcraft.network;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishDropsPayload(Map<ResourceLocation, List<ResourceLocation>> drops) implements CustomPacketPayload {
    public static final Type<WishDropsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wish_drops"), WishDropsPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, WishDropsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.RESOURCE_LOCATION, ByteBufCodecs.RESOURCE_LOCATION.apply(ByteBufCodecs.list())),
            WishDropsPayload::drops, WishDropsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
