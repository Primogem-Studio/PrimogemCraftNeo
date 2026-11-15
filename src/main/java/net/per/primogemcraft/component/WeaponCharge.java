package net.per.primogemcraft.component;

import net.per.primogemcraft.compat.component.StackComponents;
import com.mojang.serialization.Codec;
import net.per.primogemcraft.compat.network.RegistryFriendlyByteBuf;
import net.per.primogemcraft.compat.network.ByteBufCodecs;
import net.per.primogemcraft.compat.network.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;

public record WeaponCharge(int value) {
    public static final Codec<WeaponCharge> CODEC = Codec.INT.xmap(WeaponCharge::new, WeaponCharge::value);

    public static final StreamCodec<RegistryFriendlyByteBuf, WeaponCharge> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WeaponCharge::value, WeaponCharge::new);

    public static int of(ItemStack stack) {
        var charge = StackComponents.get(stack, PGCDataComponents.WEAPON_CHARGE);
        return charge == null ? 0 : charge.value();
    }

    public static void set(ItemStack stack, int value) {
        if (value <= 0) StackComponents.remove(stack, PGCDataComponents.WEAPON_CHARGE);
        else StackComponents.set(stack, PGCDataComponents.WEAPON_CHARGE, new WeaponCharge(value));
    }
}
