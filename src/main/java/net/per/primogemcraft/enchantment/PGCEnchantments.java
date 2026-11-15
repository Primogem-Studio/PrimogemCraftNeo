package net.per.primogemcraft.enchantment;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;

import java.util.Optional;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class PGCEnchantments {
    public static final RegistryKey<Enchantment> AMBROSIAL_ARBOR_ATTACHMENT = key("ambrosial_arbor_attachment");
    public static final RegistryKey<Enchantment> CAN_OPENER = key("can_opener");
    public static final RegistryKey<Enchantment> FOOLS_WRATH = key("fools_wrath");
    public static final RegistryKey<Enchantment> COSMIC_FRAGMENT_MENDING = key("cosmic_fragment_mending");
    public static final RegistryKey<Enchantment> THE_HUNT = key("the_hunt");

    private PGCEnchantments() {
    }

    private static RegistryKey<Enchantment> key(String name) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, name));
    }

    public static Optional<Enchantment> holder(World level, RegistryKey<Enchantment> enchantment) {
        return Optional.ofNullable(level.getRegistryManager().get(RegistryKeys.ENCHANTMENT).get(enchantment));
    }

    public static int levelOf(World level, ItemStack stack, RegistryKey<Enchantment> enchantment) {
        return holder(level, enchantment).map(e -> net.per.primogemcraft.compat.item.Enchantments120.level(stack, e)).orElse(0);
    }

    public static boolean supports(World level, ItemStack stack, RegistryKey<Enchantment> enchantment) {
        return holder(level, enchantment).map(e -> net.per.primogemcraft.compat.item.Enchantments120.supports(stack, e)).orElse(false);
    }

    public static void apply(World level, ItemStack stack, RegistryKey<Enchantment> enchantment, int enchantmentLevel) {
        var holder = holder(level, enchantment);
        if (holder.isEmpty() || !net.per.primogemcraft.compat.item.Enchantments120.supports(stack, holder.get())) return;
        stack.addEnchantment(holder.get(), enchantmentLevel);
    }

    public static void remove(World level, ItemStack stack, RegistryKey<Enchantment> enchantment) {
        var holder = holder(level, enchantment);
        if (holder.isEmpty() || net.per.primogemcraft.compat.item.Enchantments120.level(stack, holder.get()) <= 0) return;
        net.per.primogemcraft.compat.item.Enchantments120.update(stack, mutable -> mutable.set(holder.get(), 0));
    }
}
