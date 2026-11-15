package net.per.primogemcraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.per.primogemcraft.system.weapon.WeaponState;

public record WeaponRecovery(WeaponState state, boolean fiveStar) {
    public static final Codec<WeaponRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeaponState.CODEC.fieldOf("state").forGetter(WeaponRecovery::state),
            Codec.BOOL.fieldOf("five_star").forGetter(WeaponRecovery::fiveStar)
    ).apply(instance, WeaponRecovery::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponRecovery> STREAM_CODEC = StreamCodec.composite(
            WeaponState.STREAM_CODEC, WeaponRecovery::state,
            ByteBufCodecs.BOOL, WeaponRecovery::fiveStar,
            WeaponRecovery::new);
}
