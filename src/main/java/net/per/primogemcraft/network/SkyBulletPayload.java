package net.per.primogemcraft.network;

import net.minecraft.util.Identifier;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.CustomPacketPayload;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.compat.network.StreamCodecs;
import net.minecraft.util.math.Vec3d;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record SkyBulletPayload(int entityId, Vec3d position, Vec3d velocity) implements CustomPacketPayload {
    public static final Type<SkyBulletPayload> TYPE = new Type<>(new Identifier(MOD_ID, "sky_bullet"), SkyBulletPayload.class);
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
                    new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                    new Vec3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
