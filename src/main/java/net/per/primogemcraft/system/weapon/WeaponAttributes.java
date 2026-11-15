package net.per.primogemcraft.system.weapon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.compat.attribute.AttributesCompat;
import net.per.primogemcraft.config.PGCConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 武器等级/精炼属性加成。
 *
 * <p>1.21 版本把加成写进 {@code DataComponents.ATTRIBUTE_MODIFIERS} 组件；
 * 1.20.1 没有数据组件系统，改为直接读写原版物品 NBT 的 {@code AttributeModifiers}
 * 列表（与 {@code ItemStack.addAttributeModifier} 使用的格式完全一致：
 * {@code Name/Amount/Operation/UUID/AttributeName/Slot}）。
 *
 * <p>为保留「按 ResourceLocation 标识增删改」的语义，每个由本模组写入的条目
 * 额外携带 {@code PGCId} 标签记录原始 id；读取时据此识别，避免误删原版或其他模组条目。
 */
public final class WeaponAttributes {
    public static final EquipmentSlot SLOT = EquipmentSlot.MAINHAND;

    private static final String TAG_MODIFIERS = "AttributeModifiers";
    private static final String TAG_PGC_ID = "PGCId";

    private static final ResourceLocation LEVEL_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "weapon/level");
    private static final String PASSIVE_ROOT = "weapon/passive";
    private static final String PASSIVE_PREFIX = PASSIVE_ROOT + "/";

    private static final int FIRST_TIER = 30;
    private static final int SECOND_TIER = 60;
    private static final double FIRST_STEP = 0.01D;
    private static final double SECOND_STEP = 0.02D;
    private static final double THIRD_STEP = 0.03D;
    private static final double EQUILIBRIUM_FIRST = 1.0D;
    private static final double EQUILIBRIUM_SECOND = 1.5D;
    private static final double EQUILIBRIUM_THIRD = 2.0D;

    private WeaponAttributes() {
    }

    public static void refreshLevel(ItemStack stack, Player player) {
        var type = WeaponType.of(stack);
        write(stack, type.attribute(), levelAttackBonus(stack, player, WeaponState.of(stack).level()));
    }

    public static void refreshLevels(Player player) {
        var inventory = player.getInventory();
        for (var index = 0; index < inventory.getContainerSize(); index++) {
            var stack = inventory.getItem(index);
            if (stack.getItem() instanceof WishWeapon) refreshLevel(stack, player);
        }
    }

    public static double levelAttackBonus(ItemStack stack, Player player, int level) {
        var bonus = levelBonus(level, Equilibrium.of(player).bonus(), PGCConfig.WEAPON_DAMAGE_MULTIPLIER.get());
        return bonus * WeaponType.of(stack).coefficient();
    }

    public static void refreshPassive(ItemStack stack, Player player, int slot) {
        if (!(stack.getItem() instanceof WishWeapon weapon)) return;
        var refinement = WeaponEnhancement.refinementOf(player, stack);
        var modifiers = weapon.allPassives(player, stack, slot, refinement);
        var target = new LinkedHashMap<ResourceLocation, Applied>(modifiers.size());
        for (var index = 0; index < modifiers.size(); index++) {
            var modifier = modifiers.get(index);
            var id = ResourceLocation.fromNamespaceAndPath(MOD_ID, PASSIVE_PREFIX + index);
            target.put(id, new Applied(modifier.attribute(), modifier.amount(player, stack, refinement), modifier.operation()));
        }
        if (matches(stack, target)) return;

        var list = copyNonPassive(stack);
        for (var change : target.entrySet()) {
            list.add(entry(change.getValue().attribute(),
                    AttributesCompat.modifier(change.getKey(), change.getValue().amount(), change.getValue().operation()),
                    change.getKey()));
        }
        putList(stack, list);
    }

    public static boolean restoreDurability(ItemStack stack, int amount) {
        if (amount <= 0 || !stack.isDamageableItem()) return false;
        var damage = stack.getDamageValue();
        if (damage <= 0) return false;
        stack.setDamageValue(Math.max(0, damage - amount));
        return true;
    }

    // ------------------------------------------------------------ NBT 读写

    private static ListTag modifierList(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_MODIFIERS, Tag.TAG_LIST)) return new ListTag();
        return tag.getList(TAG_MODIFIERS, Tag.TAG_COMPOUND);
    }

    private static void putList(ItemStack stack, ListTag list) {
        stack.getOrCreateTag().put(TAG_MODIFIERS, list);
    }

    /** 复制所有「非本模组被动」的条目，保留原版与其他模组写入的加成。 */
    private static ListTag copyNonPassive(ItemStack stack) {
        var result = new ListTag();
        var list = modifierList(stack);
        for (var index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            if (!isPassive(entry)) result.add(entry);
        }
        return result;
    }

    private static CompoundTag entry(Attribute attribute, AttributeModifier modifier, ResourceLocation id) {
        var tag = modifier.save();
        tag.putString("AttributeName", BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString());
        tag.putString("Slot", SLOT.getName());
        tag.putString(TAG_PGC_ID, id.toString());
        return tag;
    }

    private static ResourceLocation pgcId(CompoundTag entry) {
        if (!entry.contains(TAG_PGC_ID, Tag.TAG_STRING)) return null;
        return ResourceLocation.tryParse(entry.getString(TAG_PGC_ID));
    }

    private static boolean isPassive(CompoundTag entry) {
        var id = pgcId(entry);
        if (id == null || !id.getNamespace().equals(MOD_ID)) return false;
        var path = id.getPath();
        return path.equals(PASSIVE_ROOT) || path.startsWith(PASSIVE_PREFIX);
    }

    private static boolean matches(ItemStack stack, Map<ResourceLocation, Applied> target) {
        var found = 0;
        var list = modifierList(stack);
        for (var index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            if (!isPassive(entry)) continue;
            var change = target.get(pgcId(entry));
            if (change == null || !same(entry, change)) return false;
            found++;
        }
        return found == target.size();
    }

    private static boolean same(CompoundTag entry, Applied change) {
        var attribute = BuiltInRegistries.ATTRIBUTE.getOptional(
                ResourceLocation.tryParse(entry.getString("AttributeName")));
        if (attribute.isEmpty() || !attribute.get().equals(change.attribute())) return false;
        if (!entry.getString("Slot").equals(SLOT.getName())) return false;
        var modifier = AttributeModifier.load(entry);
        return modifier != null && modifier.getAmount() == change.amount()
                && modifier.getOperation() == change.operation();
    }

    private static double levelBonus(int level, double equilibrium, double multiplier) {
        if (level > SECOND_TIER)
            return FIRST_TIER * multiplier * (FIRST_STEP + equilibrium)
                    + FIRST_TIER * multiplier * (SECOND_STEP + equilibrium * EQUILIBRIUM_SECOND)
                    + (level - SECOND_TIER) * multiplier * (THIRD_STEP + equilibrium * EQUILIBRIUM_THIRD);
        if (level >= FIRST_TIER)
            return FIRST_TIER * multiplier * (FIRST_STEP + equilibrium)
                    + (level - FIRST_TIER) * multiplier * (SECOND_STEP + equilibrium * EQUILIBRIUM_SECOND);
        return level * multiplier * (FIRST_STEP + equilibrium);
    }

    private static void write(ItemStack stack, Attribute attribute, double amount) {
        var list = modifierList(stack);
        var existing = find(list, LEVEL_ID);
        if (existing != null) {
            var modifier = AttributeModifier.load(existing);
            if (modifier != null && modifier.getAmount() == amount
                    && modifier.getOperation() == AttributeModifier.Operation.ADDITION
                    && existing.getString("Slot").equals(SLOT.getName())
                    && BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString()
                            .equals(existing.getString("AttributeName"))) {
                return;
            }
        }
        var result = new ListTag();
        for (var index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            if (LEVEL_ID.equals(pgcId(entry))) continue;
            result.add(entry);
        }
        result.add(entry(attribute, AttributesCompat.modifier(LEVEL_ID, amount, AttributeModifier.Operation.ADDITION), LEVEL_ID));
        putList(stack, result);
    }

    private static CompoundTag find(ListTag list, ResourceLocation id) {
        for (var index = 0; index < list.size(); index++) {
            var entry = list.getCompound(index);
            if (id.equals(pgcId(entry))) return entry;
        }
        return null;
    }

    private record Applied(Attribute attribute, double amount, AttributeModifier.Operation operation) {
    }
}
