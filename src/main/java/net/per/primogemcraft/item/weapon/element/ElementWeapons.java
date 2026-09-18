package net.per.primogemcraft.item.weapon.element;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;

public final class ElementWeapons {
    public static final double TICKS_PER_SECOND = 20.0D;

    private static final double WAX_SEAL_BOOST = 2.0D;
    private static final double WAX_SEAL_PENALTY = 0.5D;
    private static final double REFINEMENT_STEP = 0.25D;
    private static final double LAYER_OFFSET = 1.0D;

    private ElementWeapons() {
    }

    public static double scaled(Player player, ItemStack stack, Element element, double base, boolean increasing) {
        return scaled(WeaponEnhancement.refinementOf(player, stack), base, increasing, Element.holdsWaxSeal(player, element));
    }

    public static int ticks(Player player, ItemStack stack, Element element, double base, boolean increasing) {
        return (int) scaled(player, stack, element, base, increasing);
    }

    public static double scaled(int refinement, double base, boolean increasing, boolean sealed) {
        var value = sealed ? base * (increasing ? WAX_SEAL_BOOST : WAX_SEAL_PENALTY) : base;
        var layers = refinement - LAYER_OFFSET;
        var delta = layers * value * REFINEMENT_STEP;
        return increasing ? value + delta : value - delta;
    }

    public static int ticks(int refinement, double base, boolean increasing, boolean sealed) {
        return (int) scaled(refinement, base, increasing, sealed);
    }

    public static double seconds(int refinement, double base, boolean increasing, boolean sealed) {
        return ticks(refinement, base, increasing, sealed) / TICKS_PER_SECOND;
    }
}
