package net.per.primogemcraft.system.curio;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;

public record CurioImpact(CurioSignal signal, ServerWorld level, Vec3d position, Entity subject, float amount, BlockState state, ItemStack item) {
    public static CurioImpact ofBlock(CurioSignal signal, ServerWorld level, Vec3d position, BlockState state) {
        return new CurioImpact(signal, level, position, null, 0.0F, state, ItemStack.EMPTY);
    }

    public static CurioImpact ofEntity(CurioSignal signal, ServerWorld level, Vec3d position, Entity subject, float amount) {
        return new CurioImpact(signal, level, position, subject, amount, null, ItemStack.EMPTY);
    }

    public static CurioImpact ofItem(CurioSignal signal, ServerWorld level, Vec3d position, ItemStack item) {
        return new CurioImpact(signal, level, position, null, 0.0F, null, item);
    }
}
