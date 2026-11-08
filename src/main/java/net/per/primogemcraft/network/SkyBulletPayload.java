package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record SkyBulletPayload(int entityId, Vec3 position, Vec3 velocity) implements CustomPacketPayload {
    public static final Type<SkyBulletPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "sky_bullet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SkyBulletPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.entityId());
                buffer.writeDouble(payload.position().x);
                buffer.writeDouble(payload.position().y);
                buffer.writeDouble(payload.position().z);
                buffer.writeDouble(payload.velocity().x);
                buffer.writeDouble(payload.velocity().y);
                buffer.writeDouble(payload.velocity().z);
            }, buffer -> new SkyBulletPayload(buffer.readVarInt(),
                    new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
