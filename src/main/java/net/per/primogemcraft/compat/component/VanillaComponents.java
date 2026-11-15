package net.per.primogemcraft.compat.component;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public final class VanillaComponents {

    public static final String MAX_DAMAGE = "PGCMaxDamage";

    private VanillaComponents() {
    }

    public static void setCustomName(ItemStack stack, Text name) {
        stack.setCustomName(name);
    }

    public static Optional<Text> customName(ItemStack stack) {
        var tag = stack.getNbt();
        if (tag == null || !tag.contains("display", NbtElement.COMPOUND_TYPE)) return Optional.empty();
        var display = tag.getCompound("display");
        if (!display.contains("Name", NbtElement.STRING_TYPE)) return Optional.empty();
        return Optional.ofNullable(Text.Serializer.fromJson(display.getString("Name")));
    }

    public static int maxDamage(ItemStack stack) {
        var tag = stack.getNbt();
        if (tag != null && tag.contains(MAX_DAMAGE, NbtElement.INT_TYPE)) return tag.getInt(MAX_DAMAGE);
        return stack.getMaxDamage();
    }

    public static void setMaxDamage(ItemStack stack, int value) {
        stack.getOrCreateNbt().putInt(MAX_DAMAGE, value);
    }

    public static Optional<Trim> trim(ItemStack stack) {
        var tag = stack.getNbt();
        if (tag == null || !tag.contains("Trim", NbtElement.COMPOUND_TYPE)) return Optional.empty();
        var trim = tag.getCompound("Trim");
        var material = Identifier.tryParse(trim.getString("material"));
        var pattern = Identifier.tryParse(trim.getString("pattern"));
        if (material == null || pattern == null) return Optional.empty();
        return Optional.of(new Trim(material, pattern));
    }

    public record Trim(Identifier material, Identifier pattern) {
    }

    public static void setFireworks(ItemStack stack, List<FireworkExplosion> explosions, int flight) {
        var data = FireworkExplosion.saveFireworks(explosions, flight);
        stack.getOrCreateNbt().put("Fireworks", data.getCompound("Fireworks"));
    }

    public static NbtList explosionsTag(List<FireworkExplosion> explosions) {
        var list = new NbtList();
        for (var explosion : explosions) list.add(explosion.save());
        return list;
    }

    public static NbtCompound starterTag(List<FireworkExplosion> explosions) {
        var tag = new NbtCompound();
        tag.put("Explosions", explosionsTag(explosions));
        return tag;
    }

    public static void setStoredEnchantments(ItemStack target, ItemStack source) {
        var enchantments = source.getEnchantments();
        target.getOrCreateNbt().put("StoredEnchantments", enchantments.copy());
    }

    public static NbtList storedEnchantments(ItemStack stack) {
        var tag = stack.getNbt();
        if (tag == null) return new NbtList();
        return tag.getList("StoredEnchantments", NbtElement.COMPOUND_TYPE);
    }

    public static Optional<Identifier> potion(ItemStack stack) {
        var tag = stack.getNbt();
        if (tag == null || !tag.contains("Potion", NbtElement.STRING_TYPE)) return Optional.empty();
        return Optional.ofNullable(Identifier.tryParse(tag.getString("Potion")));
    }
}