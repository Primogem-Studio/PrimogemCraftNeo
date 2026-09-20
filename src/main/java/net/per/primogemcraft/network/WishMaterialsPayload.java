package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishMaterialsPayload(Map<ResourceLocation, Integer> values) implements CustomPacketPayload {
    public static final Type<WishMaterialsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wish_materials"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WishMaterialsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT),
            WishMaterialsPayload::values, WishMaterialsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
