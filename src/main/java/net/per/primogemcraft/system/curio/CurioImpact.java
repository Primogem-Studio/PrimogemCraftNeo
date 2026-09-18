package net.per.primogemcraft.system.curio;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record CurioImpact(CurioSignal signal, ServerLevel level, Vec3 position, Entity subject, float amount, BlockState state, ItemStack item) {
    public static CurioImpact ofBlock(CurioSignal signal, ServerLevel level, Vec3 position, BlockState state) {
        return new CurioImpact(signal, level, position, null, 0.0F, state, ItemStack.EMPTY);
    }

    public static CurioImpact ofEntity(CurioSignal signal, ServerLevel level, Vec3 position, Entity subject, float amount) {
        return new CurioImpact(signal, level, position, subject, amount, null, ItemStack.EMPTY);
    }

    public static CurioImpact ofItem(CurioSignal signal, ServerLevel level, Vec3 position, ItemStack item) {
        return new CurioImpact(signal, level, position, null, 0.0F, null, item);
    }
}
