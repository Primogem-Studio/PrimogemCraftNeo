package net.per.primogemcraft.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;
import net.per.primogemcraft.system.menu.ContainerWindowSound;

/**
 * The parts of the container window behaviour that every container block shares, whatever block class it extends.
 */
public final class ContainerWindowBlocks {
    private ContainerWindowBlocks() {
    }

    public static void open(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof ContainerWindowBlockEntity window) {
            serverPlayer.openMenu(window);
            ContainerWindowSound.TRASH_CAN_OPEN.play(level, pos.getX(), pos.getY(), pos.getZ(), level.getRandom());
        }
    }

    public static void dropContents(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof ContainerWindowBlockEntity window)) return;
        Containers.dropContents(level, pos, window);
        level.updateNeighbourForOutputSignal(pos, level.getBlockState(pos).getBlock());
    }

    public static int size(BlockState state) {
        return state.getBlock() instanceof ContainerWindowBlock window ? window.containerSize() : 0;
    }
}
