package net.per.primogemcraft.system.weapon;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public record WeaponModifier(EntityAttribute attribute, double base, double perRefinement, EntityAttributeModifier.Operation operation, Value value) {
    @FunctionalInterface
    public interface Value {
        double of(PlayerEntity player, ItemStack stack, int refinement);
    }

    public static WeaponModifier of(EntityAttribute attribute, double base, double perRefinement, EntityAttributeModifier.Operation operation) {
        return new WeaponModifier(attribute, base, perRefinement, operation, null);
    }

    public static WeaponModifier conditional(EntityAttribute attribute, EntityAttributeModifier.Operation operation, Value value) {
        return new WeaponModifier(attribute, 0.0D, 0.0D, operation, value);
    }

    public double amount(int refinement) {
        return base + perRefinement * (refinement - 1);
    }

    public double amount(PlayerEntity player, ItemStack stack, int refinement) {
        return value == null ? amount(refinement) : value.of(player, stack, refinement);
    }
}
