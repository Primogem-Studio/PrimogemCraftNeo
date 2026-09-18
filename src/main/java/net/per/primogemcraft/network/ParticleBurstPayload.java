package net.per.primogemcraft.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public record ParticleBurstPayload(int kind, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<ParticleBurstPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "particle_burst"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleBurstPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ParticleBurstPayload::kind, ByteBufCodecs.DOUBLE, ParticleBurstPayload::x, ByteBufCodecs.DOUBLE, ParticleBurstPayload::y, ByteBufCodecs.DOUBLE, ParticleBurstPayload::z, ParticleBurstPayload::new);

    public static ParticleBurstPayload at(int kind, Entity entity) {
        return new ParticleBurstPayload(kind, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
