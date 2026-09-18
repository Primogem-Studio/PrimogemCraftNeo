package net.per.primogemcraft.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class PGCEnchantments {
    public static final ResourceKey<Enchantment> AMBROSIAL_ARBOR_ATTACHMENT = key("ambrosial_arbor_attachment");
    public static final ResourceKey<Enchantment> CAN_OPENER = key("can_opener");
    public static final ResourceKey<Enchantment> FOOLS_WRATH = key("fools_wrath");
    public static final ResourceKey<Enchantment> COSMIC_FRAGMENT_MENDING = key("cosmic_fragment_mending");
    public static final ResourceKey<Enchantment> THE_HUNT = key("the_hunt");

    private PGCEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, name));
    }

    public static Optional<Holder.Reference<Enchantment>> holder(Level level, ResourceKey<Enchantment> enchantment) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment);
    }

    public static int levelOf(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        return holder(level, enchantment).map(stack::getEnchantmentLevel).orElse(0);
    }

    public static boolean supports(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        return holder(level, enchantment).map(stack::supportsEnchantment).orElse(false);
    }

    public static void apply(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment, int enchantmentLevel) {
        var holder = holder(level, enchantment);
        if (holder.isEmpty() || !stack.supportsEnchantment(holder.get())) return;
        stack.enchant(holder.get(), enchantmentLevel);
    }

    public static void remove(Level level, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        var holder = holder(level, enchantment);
        if (holder.isEmpty() || stack.getEnchantmentLevel(holder.get()) <= 0) return;
        EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(holder.get(), 0));
    }
}
