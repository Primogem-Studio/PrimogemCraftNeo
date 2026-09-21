package net.per.primogemcraft.system.living;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.entity.misc.LivingItemDrop;

import java.util.Set;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class LivingItemEvents {
    @SubscribeEvent
    public static void onOwnerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner)) return;
        var previousLevel = owner.serverLevel().getServer().getLevel(event.getFrom());
        if (previousLevel == null) return;
        var test = EntityTypeTest.<Entity, LivingItemEntity>forClass(LivingItemEntity.class);
        var items = previousLevel.getEntities(test, entity -> owner.getUUID().equals(entity.getOwnerUuid()) && entity.isAlive());
        for (var entity : items) {
            entity.teleportTo(owner.serverLevel(), owner.getX(), owner.getY() + 2.5, owner.getZ(), Set.of(), owner.getYRot(), 0);
        }
    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Post event) {
        if (event.getItemEntity() instanceof LivingItemDrop drop) drop.setInvulnerable(false);
    }

    @SubscribeEvent
    public static void onOwnerAttack(AttackEntityEvent event) {
        var owner = event.getEntity();
        if (owner == null || owner.level().isClientSide) return;
        for (var entity : LivingItemAPI.collectAll(owner)) entity.onOwnerAttack(event.getTarget());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getTarget() instanceof LivingItemEntity entity)) return;
        var player = event.getEntity();
        if (!player.isCrouching() || !player.getMainHandItem().isEmpty()) return;
        if (entity.getOwnerUuid() == null || !entity.getOwnerUuid().equals(player.getUUID())) return;
        entity.revertByOwner();
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
