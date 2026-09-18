package net.per.primogemcraft.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
