package net.per.primogemcraft.system.weapon;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record WeaponModifier(Holder<Attribute> attribute, double base, double perRefinement, AttributeModifier.Operation operation, Value value) {
    @FunctionalInterface
    public interface Value {
        double of(Player player, ItemStack stack, int refinement);
    }

    public static WeaponModifier of(Holder<Attribute> attribute, double base, double perRefinement, AttributeModifier.Operation operation) {
        return new WeaponModifier(attribute, base, perRefinement, operation, null);
    }

    public static WeaponModifier conditional(Holder<Attribute> attribute, AttributeModifier.Operation operation, Value value) {
        return new WeaponModifier(attribute, 0.0D, 0.0D, operation, value);
    }

    public double amount(int refinement) {
        return base + perRefinement * (refinement - 1);
    }

    public double amount(Player player, ItemStack stack, int refinement) {
        return value == null ? amount(refinement) : value.of(player, stack, refinement);
    }
}
