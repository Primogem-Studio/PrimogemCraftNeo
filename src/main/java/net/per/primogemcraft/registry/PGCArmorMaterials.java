package net.per.primogemcraft.registry;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * Armour materials.
 *
 * <p>1.21 turned {@link ArmorMaterial} into a registry record; 1.20.1 still uses
 * an interface implemented by an enum-like object, and there is no
 * {@code Registries.ARMOR_MATERIAL}. The materials are therefore plain constants
 * here. Defence values, enchantability, equip sounds, repair ingredients,
 * toughness and knockback resistance are carried over unchanged from the 1.21
 * definitions; durability uses the vanilla per-slot base factors times the
 * original multiplier.
 */
public final class PGCArmorMaterials {

    /** Vanilla 1.20.1 base durability per slot (ArmorMaterials.HEALTH_FUNCTION_FOR_TYPE). */
    private static final Map<ArmorItem.Type, Integer> BASE_DURABILITY = new EnumMap<>(Map.of(
            ArmorItem.Type.BOOTS, 13,
            ArmorItem.Type.LEGGINGS, 15,
            ArmorItem.Type.CHESTPLATE, 16,
            ArmorItem.Type.HELMET, 11));

    public static final ArmorMaterial COLORFUL_SUNGLASSES = new Simple(
            "colorful_sunglasses",
            Map.of(ArmorItem.Type.HELMET, 2),
            15, 15,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            () -> Ingredient.ofItems(PGCItems.PRIMOGEM.get()),
            0.0F, 0.0F);

    public static final ArmorMaterial SPECIAL_01 = new Simple(
            "special_01",
            Map.of(ArmorItem.Type.HELMET, 1),
            1, 1,
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            Ingredient::ofItems,
            0.0F, 0.0F);

    public static final ArmorMaterial MORA = new Simple(
            "mora",
            Map.of(ArmorItem.Type.HELMET, 5,
                    ArmorItem.Type.CHESTPLATE, 10,
                    ArmorItem.Type.LEGGINGS, 8,
                    ArmorItem.Type.BOOTS, 5),
            64, 20,
            SoundEvents.INTENTIONALLY_EMPTY,
            () -> Ingredient.ofItems(PGCItems.REFINED_MORA.get()),
            4.0F, 0.2F);

    private PGCArmorMaterials() {
    }

    /** 1.20.1 ArmorMaterial implementation backed by the 1.21 record's fields. */
    private record Simple(String name,
                          Map<ArmorItem.Type, Integer> defence,
                          int durabilityMultiplier,
                          int enchantmentValue,
                          SoundEvent equipSound,
                          Supplier<Ingredient> repairIngredient,
                          float toughness,
                          float knockbackResistance) implements ArmorMaterial {

        @Override
        public int getDurability(ArmorItem.Type type) {
            return BASE_DURABILITY.getOrDefault(type, 0) * durabilityMultiplier;
        }

        @Override
        public int getProtection(ArmorItem.Type type) {
            return defence.getOrDefault(type, 0);
        }

        @Override
        public int getEnchantability() {
            return enchantmentValue;
        }

        @Override
        public SoundEvent getEquipSound() {
            return equipSound;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient.get();
        }

        @Override
        public String getName() {
            return MOD_ID + ":" + name;
        }

        @Override
        public float getToughness() {
            return toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return knockbackResistance;
        }
    }
}
