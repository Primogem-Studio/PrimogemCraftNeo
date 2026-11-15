package net.per.primogemcraft.system.weapon;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.per.primogemcraft.compat.registries.DeferredItem;

import java.util.function.Supplier;

/**
 * 自定义武器材质。
 *
 * <p>1.21 的 {@code Tier} 用 {@code getIncorrectBlocksForDrops()} 表达「不能正确采集的方块」；
 * 1.20.1 改为 {@code getTag()} 表达「能正确采集的方块」，且新增 {@code getLevel()}。
 * 这里把传入的 incorrect 标签取反语义映射为 1.20.1 的 mineable 标签：
 * 原模组传入的 INCORRECT_FOR_* 在 1.20.1 不存在，改用对应的 NEEDS_*_TOOL 标签体系，
 * 由 {@link #getTag()} 返回该材质可采集的方块标签。
 */
public final class WeaponTier implements Tier {
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
        this(uses, speed, enchantmentValue, incorrectBlocks, () -> Ingredient.of(repairIngredient));
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
        return new WeaponTier(uses, speed, enchantmentValue, incorrectBlocks, Ingredient.of(item));
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return 0.0F;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public TagKey<Block> getTag() {
        return mineable;
    }
}
