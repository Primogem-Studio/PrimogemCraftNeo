package net.per.primogemcraft.enchantment;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.system.curio.CurioLoot;

public final class CanOpener {
    private static final double EXTRA_LOOT_CHANCE_PER_LEVEL = 0.1D;

    private CanOpener() {
    }

    public static void pryOpen(ServerPlayerEntity player, BlockState state, Vec3d position) {
        var tool = player.getMainHandStack();
        var level = PGCEnchantments.levelOf(player.getWorld(), tool, PGCEnchantments.CAN_OPENER);
        if (level <= 0 || player.getRandom().nextDouble() >= level * EXTRA_LOOT_CHANCE_PER_LEVEL) return;
        CurioLoot.dropTable(CurioLoot.jarLoot(state), player.getServerWorld(), position, state, player);
        tool.damage(1, player, r -> r.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
    }
}
