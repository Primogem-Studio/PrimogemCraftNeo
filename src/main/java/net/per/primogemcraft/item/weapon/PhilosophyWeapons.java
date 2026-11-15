package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
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
                WishReports.percent(CHANCE_BASE + CHANCE_STEP * layers, Formatting.AQUA),
                WishReports.number(1 + layers, Formatting.AQUA));
    }

    static void drop(ServerWorld level, Vec3d pos, PlayerEntity player, ItemStack stack) {
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return;
        var layers = WeaponEnhancement.refinementOf(player, stack) - 1;
        if (level.getRandom().nextDouble() >= CHANCE_BASE + CHANCE_STEP * layers) return;
        var mora = new ItemStack(PGCItems.MORA.get(), MathHelper.nextInt(level.getRandom(), 1, 1 + layers));
        var dropped = new ItemEntity(level, pos.x, pos.y, pos.z, mora);
        dropped.setPickupDelay(PICKUP_DELAY);
        level.spawnEntity(dropped);
        player.getItemCooldownManager().set(stack.getItem(), COOLDOWN_TICKS);
    }
}
