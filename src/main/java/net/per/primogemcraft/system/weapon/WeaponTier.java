package net.per.primogemcraft.system.weapon;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public final class WeaponTier implements Tier {
    public static final TagKey<Block> WOODEN_INCORRECT = BlockTags.INCORRECT_FOR_WOODEN_TOOL;
    public static final TagKey<Block> STONE_INCORRECT = BlockTags.INCORRECT_FOR_STONE_TOOL;
    public static final TagKey<Block> IRON_INCORRECT = BlockTags.INCORRECT_FOR_IRON_TOOL;
    public static final TagKey<Block> DIAMOND_INCORRECT = BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    public static final TagKey<Block> NETHERITE_INCORRECT = BlockTags.INCORRECT_FOR_NETHERITE_TOOL;

    private final int uses;
    private final float speed;
    private final int enchantmentValue;
    private final TagKey<Block> incorrectBlocks;
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
        this.incorrectBlocks = incorrectBlocks;
        this.repairIngredient = repairIngredient;
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
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocks;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }
}
