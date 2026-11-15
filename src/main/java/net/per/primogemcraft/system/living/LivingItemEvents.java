package net.per.primogemcraft.system.living;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.per.primogemcraft.entity.misc.LivingItemDrop;
import net.per.primogemcraft.entity.mob.LivingItemEntity;

import java.util.Set;

public final class LivingItemEvents {
    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            for (var item : LivingItemAPI.collectAll(player)) item.onOwnerAttack(entity);
            return ActionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;
            if (!(entity instanceof LivingItemEntity livingItem)) return ActionResult.PASS;
            if (!player.isSneaking() || !player.getMainHandStack().isEmpty()) return ActionResult.PASS;
            if (livingItem.getOwnerUuid() == null || !livingItem.getOwnerUuid().equals(player.getUuid())) return ActionResult.PASS;
            livingItem.revertByOwner();
            return ActionResult.SUCCESS;
        });
    }
}