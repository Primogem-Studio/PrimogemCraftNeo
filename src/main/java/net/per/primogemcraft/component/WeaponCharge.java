package net.per.primogemcraft.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;

public record WeaponCharge(int value) {
    public static final Codec<WeaponCharge> CODEC = Codec.INT.xmap(WeaponCharge::new, WeaponCharge::value);

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponCharge> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WeaponCharge::value, WeaponCharge::new);

    public static int of(ItemStack stack) {
        var charge = stack.get(PGCDataComponents.WEAPON_CHARGE.get());
        return charge == null ? 0 : charge.value();
    }

    public static void set(ItemStack stack, int value) {
        if (value <= 0) stack.remove(PGCDataComponents.WEAPON_CHARGE.get());
        else stack.set(PGCDataComponents.WEAPON_CHARGE.get(), new WeaponCharge(value));
    }
}
