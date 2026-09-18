package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.wish.WishReports;

final class PhilosophyWeapons {
    static final String PASSIVE_ACTION = "passive";
    static final String MORA_TEXT = "mora";

    private static final double CHANCE_BASE = 0.06D;
    private static final double CHANCE_STEP = 0.015D;
    private static final int COOLDOWN_TICKS = 20;
    private static final int PICKUP_DELAY = 10;

    private PhilosophyWeapons() {
    }

    static WeaponDescription moraDescription(ItemStack stack) {
        var layers = WeaponState.of(stack).refinements() - 1;
        return WeaponDescription.of(PASSIVE_ACTION, MORA_TEXT,
                WishReports.percent(CHANCE_BASE + CHANCE_STEP * layers, ChatFormatting.AQUA),
                WishReports.number(1 + layers, ChatFormatting.AQUA));
    }

    static void drop(ServerLevel level, Vec3 pos, Player player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
        var layers = WeaponEnhancement.refinementOf(player, stack) - 1;
        if (level.getRandom().nextDouble() >= CHANCE_BASE + CHANCE_STEP * layers) return;
        var mora = new ItemStack(PGCItems.MORA.get(), Mth.nextInt(level.getRandom(), 1, 1 + layers));
        var dropped = new ItemEntity(level, pos.x, pos.y, pos.z, mora);
        dropped.setPickUpDelay(PICKUP_DELAY);
        level.addFreshEntity(dropped);
        player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
    }
}
