package net.per.primogemcraft.compat.item;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;

import java.util.UUID;

/**
 * Attack attribute helpers for 1.20.1.
 *
 * <p>1.21 weapon constructors take the final attack damage as a {@code float}
 * (properties-based attributes). 1.20.1 constructors take an {@code int} bonus
 * that is added to {@code Tier.getAttackDamage()}, so fractional values
 * such as 4.5 or -0.7 cannot be expressed through the constructor.
 *
 * <p>To keep the original numbers exactly, weapons pass 0 to the super
 * constructor and then rebuild the two vanilla attribute modifiers here with
 * the intended float values. The UUIDs match the ones vanilla uses, so the
 * modifiers replace rather than stack.
 */
public final class WeaponAttributes120 {

    /** Same UUIDs vanilla 1.20.1 uses for its base weapon modifiers. */
    public static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    public static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    private WeaponAttributes120() {
    }

    /**
     * Replaces the base attack damage / speed modifiers on a freshly built weapon.
     *
     * @param modifiers   the mutable multimap being assembled by the item
     * @param attackDamage final attack damage exactly as the 1.21 source specified
     * @param attackSpeed  final attack speed modifier (already the negative offset)
     */
    public static void putWeaponModifiers(Multimap<EntityAttribute, EntityAttributeModifier> modifiers,
                                          float attackDamage, float attackSpeed) {
        modifiers.removeAll(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        modifiers.removeAll(EntityAttributes.GENERIC_ATTACK_SPEED);
        modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier", attackDamage, EntityAttributeModifier.Operation.ADDITION));
        modifiers.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Weapon modifier", attackSpeed, EntityAttributeModifier.Operation.ADDITION));
    }

    /** Convenience for items that only need to expose the two values. */
    public static void apply(Item.Settings properties, ToolMaterial tier) {
        // no-op: kept so call sites can document intent without side effects
    }
}
