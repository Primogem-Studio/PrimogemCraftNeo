package net.per.primogemcraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ItemScatterer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.per.primogemcraft.block.entity.ContainerWindowBlockEntity;
import net.per.primogemcraft.system.menu.ContainerWindowSound;

/**
 * The parts of the container window behaviour that every container block shares, whatever block class it extends.
 */
public final class ContainerWindowBlocks {
    private ContainerWindowBlocks() {
    }

    public static void open(World level, BlockPos pos, PlayerEntity player) {
        if (level.isClient()) return;
        if (player instanceof ServerPlayerEntity serverPlayer && level.getBlockEntity(pos) instanceof ContainerWindowBlockEntity window) {
            serverPlayer.openHandledScreen(window);
            ContainerWindowSound.TRASH_CAN_OPEN.play(level, pos.getX(), pos.getY(), pos.getZ(), level.getRandom());
        }
    }

    public static void dropContents(World level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof ContainerWindowBlockEntity window)) return;
        ItemScatterer.spawn(level, pos, window);
        level.updateComparators(pos, level.getBlockState(pos).getBlock());
    }

    public static int size(BlockState state) {
        return state.getBlock() instanceof ContainerWindowBlock window ? window.containerSize() : 0;
    }
}
