package net.per.primogemcraft.compat.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * 1.21 原版数据组件（{@code DataComponents.*}）在 1.20.1 上的等价访问。
 *
 * <p>1.20.1 没有数据组件系统，这些值分别落在物品 NBT 或原版方法上：
 * <ul>
 *   <li>{@code CUSTOM_NAME} → {@code display.Name}（{@code setHoverName}）</li>
 *   <li>{@code MAX_DAMAGE} → {@code Damage} 相关的最大耐久（原版由 Item 决定，
 *       模组自定义耐久用 {@code MaxDamage} 标签记录）</li>
 *   <li>{@code TRIM} → {@code Trim} 子标签（1.20.1 已有纹饰系统）</li>
 *   <li>{@code FIREWORKS} → {@code Fireworks} 子标签</li>
 *   <li>{@code STORED_ENCHANTMENTS} → {@code StoredEnchantments} 列表</li>
 * </ul>
 */
public final class VanillaComponents {

    /** 模组自定义最大耐久使用的 NBT 键（原版 1.20.1 无对应组件）。 */
    public static final String MAX_DAMAGE = "PGCMaxDamage";

    private VanillaComponents() {
    }

    // ------------------------------------------------------------ CUSTOM_NAME

    public static void setCustomName(ItemStack stack, Component name) {
        stack.setHoverName(name);
    }

    public static Optional<Component> customName(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains("display", Tag.TAG_COMPOUND)) return Optional.empty();
        var display = tag.getCompound("display");
        if (!display.contains("Name", Tag.TAG_STRING)) return Optional.empty();
        return Optional.ofNullable(Component.Serializer.fromJson(display.getString("Name")));
    }

    // ------------------------------------------------------------ MAX_DAMAGE

    public static int maxDamage(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains(MAX_DAMAGE, Tag.TAG_INT)) return tag.getInt(MAX_DAMAGE);
        return stack.getMaxDamage();
    }

    public static void setMaxDamage(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(MAX_DAMAGE, value);
    }

    // ------------------------------------------------------------ TRIM

    /** 1.20.1 的纹饰存在 {@code Trim} 子标签，含 material 与 pattern 两个注册名。 */
    public static Optional<Trim> trim(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains("Trim", Tag.TAG_COMPOUND)) return Optional.empty();
        var trim = tag.getCompound("Trim");
        var material = ResourceLocation.tryParse(trim.getString("material"));
        var pattern = ResourceLocation.tryParse(trim.getString("pattern"));
        if (material == null || pattern == null) return Optional.empty();
        return Optional.of(new Trim(material, pattern));
    }

    /** 纹饰信息（注册名形式，避免依赖 Holder 查询）。 */
    public record Trim(ResourceLocation material, ResourceLocation pattern) {
    }

    // ------------------------------------------------------------ FIREWORKS

    public static void setFireworks(ItemStack stack, List<FireworkExplosion> explosions, int flight) {
        var data = FireworkExplosion.saveFireworks(explosions, flight);
        stack.getOrCreateTag().put("Fireworks", data.getCompound("Fireworks"));
    }

    /** 供粒子系统使用的爆炸 NBT 列表。 */
    public static ListTag explosionsTag(List<FireworkExplosion> explosions) {
        var list = new ListTag();
        for (var explosion : explosions) list.add(explosion.save());
        return list;
    }

    /** 1.20.1 的 FireworkParticles.Starter 需要一个含 Explosions 的 CompoundTag。 */
    public static CompoundTag starterTag(List<FireworkExplosion> explosions) {
        var tag = new CompoundTag();
        tag.put("Explosions", explosionsTag(explosions));
        return tag;
    }

    // ------------------------------------------------------------ STORED_ENCHANTMENTS

    /** 把来源物品的附魔写入目标的 StoredEnchantments（附魔书语义）。 */
    public static void setStoredEnchantments(ItemStack target, ItemStack source) {
        var enchantments = source.getEnchantmentTags();
        target.getOrCreateTag().put("StoredEnchantments", enchantments.copy());
    }

    /** 读取 StoredEnchantments。 */
    public static ListTag storedEnchantments(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null) return new ListTag();
        return tag.getList("StoredEnchantments", Tag.TAG_COMPOUND);
    }

    // ------------------------------------------------------------ POTION

    /** 1.21 的 POTION_CONTENTS.potion() → 1.20.1 的 Potion 标签。 */
    public static Optional<ResourceLocation> potion(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains("Potion", Tag.TAG_STRING)) return Optional.empty();
        return Optional.ofNullable(ResourceLocation.tryParse(tag.getString("Potion")));
    }
}
