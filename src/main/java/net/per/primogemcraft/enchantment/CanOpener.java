package net.per.primogemcraft.enchantment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.system.curio.CurioLoot;

public final class CanOpener {
    private static final double EXTRA_LOOT_CHANCE_PER_LEVEL = 0.1D;

    private CanOpener() {
    }

    public static void pryOpen(ServerPlayer player, BlockState state, Vec3 position) {
        var tool = player.getMainHandItem();
        var level = PGCEnchantments.levelOf(player.level(), tool, PGCEnchantments.CAN_OPENER);
        if (level <= 0 || player.getRandom().nextDouble() >= level * EXTRA_LOOT_CHANCE_PER_LEVEL) return;
        CurioLoot.dropTable(CurioLoot.jarLoot(state), player.serverLevel(), position, state, player);
        tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
