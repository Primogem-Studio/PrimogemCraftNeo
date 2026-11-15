package net.per.primogemcraft.network;

import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record WishMaterialsPayload(Map<Identifier, Integer> values) implements CustomPacketPayload {
    public static final Type<WishMaterialsPayload> TYPE = new Type<>(Identifier.of(MOD_ID, "wish_materials"), WishMaterialsPayload.class);
    public static final StreamCodec<RegistryFriendlyByteBuf, WishMaterialsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new, ByteBufCodecs.RESOURCE_LOCATION, ByteBufCodecs.VAR_INT),
            WishMaterialsPayload::values, WishMaterialsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
