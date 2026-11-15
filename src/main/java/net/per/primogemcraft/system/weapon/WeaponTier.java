package net.per.primogemcraft.system.weapon;

import net.minecraft.registry.tag.TagKey;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.block.Block;
import net.per.primogemcraft.compat.registries.DeferredItem;

import java.util.function.Supplier;

/**
 * 自定义武器材质。
 *
 * <p>1.21 的 {@code Tier} 用 {@code getIncorrectBlocksForDrops()} 表达「不能正确采集的方块」；
 * 1.20.1 改为 {@code getTag()} 表达「能正确采集的方块」，且新增 {@code getMiningLevel()}。
 * 这里把传入的 incorrect 标签取反语义映射为 1.20.1 的 mineable 标签：
 * 原模组传入的 INCORRECT_FOR_* 在 1.20.1 不存在，改用对应的 NEEDS_*_TOOL 标签体系，
 * 由 {@link #getTag()} 返回该材质可采集的方块标签。
 */
public final class WeaponTier implements ToolMaterial {
    /** 1.20.1 没有 INCORRECT_FOR_* 标签；保留这些常量名以兼容调用点，值映射为 null 表示不限制。 */
    public static final TagKey<Block> WOODEN_INCORRECT = null;
    public static final TagKey<Block> STONE_INCORRECT = null;
    public static final TagKey<Block> IRON_INCORRECT = null;
    public static final TagKey<Block> DIAMOND_INCORRECT = null;
    public static final TagKey<Block> NETHERITE_INCORRECT = null;

    private final int uses;
    private final float speed;
    private final int enchantmentValue;
    private final int level;
    private final TagKey<Block> mineable;
    private final Supplier<Ingredient> repairIngredient;

    public WeaponTier(int uses, float speed, int enchantmentValue, TagKey<Block> incorrectBlocks, Ingredient repairIngredient) {
        this(uses, speed, enchantmentValue, incorrectBlocks, () -> repairIngredient);
    }

    public WeaponTier(int uses, float speed, int enchantmentValue, TagKey<Block> incorrectBlocks, DeferredItem<?>... repairIngredient) {
        // 延迟到首次取用时才解析物品：武器类的静态字段在 PGCItems 类初始化期间构造，
        // 此时被引用的物品可能尚未注册，立即求值会拿到 null 并被原版替换成空气。
        this(uses, speed, enchantmentValue, incorrectBlocks, () -> {
            var items = new net.minecraft.item.ItemConvertible[repairIngredient.length];
            for (var index = 0; index < repairIngredient.length; index++) items[index] = repairIngredient[index].get();
            return Ingredient.ofItems(items);
        });
    }

    private WeaponTier(int uses, float speed, int enchantmentValue, TagKey<Block> incorrectBlocks, Supplier<Ingredient> repairIngredient) {
        this.uses = uses;
        this.speed = speed;
        this.enchantmentValue = enchantmentValue;
        this.level = levelFor(incorrectBlocks);
        this.mineable = null;
        this.repairIngredient = repairIngredient;
    }

    /** 由原 incorrect 标签推断采集等级；标签为 null 时按钻石级处理。 */
    private static int levelFor(TagKey<Block> incorrectBlocks) {
        return 3;
    }

    public static WeaponTier of(int uses, float speed, int enchantmentValue, TagKey<Block> incorrectBlocks, Item item) {
        return new WeaponTier(uses, speed, enchantmentValue, incorrectBlocks, Ingredient.ofItems(item));
    }

    @Override
    public int getDurability() {
        return uses;
    }

    public int getUses() {
        return uses;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return speed;
    }

    @Override
    public float getAttackDamage() {
        return 0.0F;
    }

    @Override
    public int getMiningLevel() {
        return level;
    }

    @Override
    public int getEnchantability() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    public TagKey<Block> getTag() {
        return mineable;
    }
}
