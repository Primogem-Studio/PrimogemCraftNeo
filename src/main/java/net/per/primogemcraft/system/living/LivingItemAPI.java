package net.per.primogemcraft.system.living;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.TypeFilter;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.entity.misc.LivingItemDrop;
import net.per.primogemcraft.registry.PGCEntities;

import java.util.ArrayList;
import java.util.List;

public final class LivingItemAPI {
    private static final int DEFAULT_TICKS = 1200;

    private LivingItemAPI() {
    }

    /** Summons a living item entity from the player's offhand for {@code ticks} ticks; a negative value lasts forever. */
    public static LivingItemEntity summon(PlayerEntity player, int ticks) {
        if (player == null) return null;
        return summon(player.getWorld(), player, player.getOffHandStack(), ticks, true);
    }

    /** Summons a living item entity from the player's offhand, forever when {@code infinite}, otherwise for the default duration. */
    public static LivingItemEntity summon(PlayerEntity player, boolean infinite) {
        if (player == null) return null;
        return summon(player.getWorld(), player, player.getOffHandStack(), infinite ? -1 : DEFAULT_TICKS, true);
    }

    /** Summons a living item entity from the player's offhand in the given level for {@code ticks} ticks. */
    public static LivingItemEntity summon(World level, PlayerEntity player, int ticks) {
        if (player == null) return null;
        return summon(level, player, player.getOffHandStack(), ticks, true);
    }

    /** Summons a living item entity from the player's offhand in the given level, forever when {@code infinite}. */
    public static LivingItemEntity summon(World level, PlayerEntity player, boolean infinite) {
        if (player == null) return null;
        return summon(level, player, player.getOffHandStack(), infinite ? -1 : DEFAULT_TICKS, true);
    }

    /** Summons a living item entity of the given stack in the given level for {@code ticks} ticks. */
    public static LivingItemEntity summon(World level, PlayerEntity player, ItemStack stack, int ticks) {
        return summon(level, player, stack, ticks, false);
    }

    /** Summons a living item entity of the given stack in the given level, forever when {@code infinite}. */
    public static LivingItemEntity summon(World level, PlayerEntity player, ItemStack stack, boolean infinite) {
        return summon(level, player, stack, infinite ? -1 : DEFAULT_TICKS, false);
    }

    /** Summons a living item entity that lasts forever from the player's offhand. */
    public static LivingItemEntity summonInfinite(PlayerEntity player) {
        if (player == null) return null;
        return summon(player.getWorld(), player, player.getOffHandStack(), -1, true);
    }

    /** Summons a living item entity that lasts forever from the player's offhand in the given level. */
    public static LivingItemEntity summonInfinite(World level, PlayerEntity player) {
        if (player == null) return null;
        return summon(level, player, player.getOffHandStack(), -1, true);
    }

    /** Summons a living item entity of the given stack that lasts forever. */
    public static LivingItemEntity summonInfinite(World level, PlayerEntity player, ItemStack stack) {
        return summon(level, player, stack, -1, false);
    }

    /** Turns every item of the player's inventory and offhand into a living item entity for {@code ticks} ticks and returns how many were summoned. */
    public static int summonAllFromInventory(PlayerEntity player, int ticks) {
        if (player == null || player.getWorld().isClient) return 0;
        var count = 0;
        var inventory = player.getInventory();
        for (var index = 0; index < inventory.main.size(); index++) {
            var stack = inventory.main.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.getWorld(), player, stack, ticks, false) != null) {
                inventory.setStack(index, ItemStack.EMPTY);
                count++;
            }
        }
        var offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && summon(player.getWorld(), player, offhand, ticks, false) != null) {
            player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            count++;
        }
        return count;
    }

    /** Turns every item of the player's main inventory, except the hotbar, into a living item entity and returns how many were summoned. */
    public static int summonMainSlots(PlayerEntity player, int ticks) {
        if (player == null || player.getWorld().isClient) return 0;
        var count = 0;
        var inventory = player.getInventory();
        for (var index = 9; index < inventory.main.size(); index++) {
            var stack = inventory.main.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.getWorld(), player, stack, ticks, false) != null) {
                inventory.setStack(index, ItemStack.EMPTY);
                count++;
            }
        }
        return count;
    }

    /** Turns every item of the player's inventory, including the hotbar, armor slots and offhand, into a living item entity and returns how many were summoned. */
    public static int summonAllItems(PlayerEntity player, int ticks) {
        if (player == null || player.getWorld().isClient) return 0;
        var count = 0;
        var inventory = player.getInventory();
        for (var index = 0; index < inventory.main.size(); index++) {
            var stack = inventory.main.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.getWorld(), player, stack, ticks, false) != null) {
                inventory.setStack(index, ItemStack.EMPTY);
                count++;
            }
        }
        for (var index = 0; index < inventory.armor.size(); index++) {
            var stack = inventory.armor.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.getWorld(), player, stack, ticks, false) != null) {
                inventory.armor.set(index, ItemStack.EMPTY);
                count++;
            }
        }
        var offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && summon(player.getWorld(), player, offhand, ticks, false) != null) {
            player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            count++;
        }
        return count;
    }

    /** Moves every dropped living item of the player back to the player's position and returns how many were moved. */
    public static int recallDrops(PlayerEntity player) {
        if (player == null || player.getWorld().isClient) return 0;
        var serverLevel = (ServerWorld) player.getWorld();
        TypeFilter<Entity, LivingItemDrop> test = TypeFilter.instanceOf(LivingItemDrop.class);
        var count = 0;
        for (var drop : serverLevel.getEntitiesByType(test, entity -> player.getUuid().equals(entity.getOwnerUuid()))) {
            if (drop.isRemoved() || !drop.isAlive()) continue;
            drop.requestTeleport(player.getX(), player.getY() + 0.5, player.getZ());
            count++;
        }
        return count;
    }

    /** Sets the remaining duration of every living item of the owner to {@code ticks} and returns how many were affected. */
    public static int setDurationForAll(PlayerEntity owner, int ticks) {
        var items = collectAll(owner);
        for (var entity : items) entity.setRemainingTicks(ticks);
        return items.size();
    }

    /** Adds {@code deltaTicks} to the remaining duration of the owner's finite living items and returns how many were affected. */
    public static int addDurationForAll(PlayerEntity owner, int deltaTicks) {
        var items = collectAll(owner);
        var count = 0;
        for (var entity : items) {
            if (entity.isInfinite()) continue;
            entity.addRemainingTicks(deltaTicks);
            count++;
        }
        return count;
    }

    /** Collects every living item of the owner that is currently alive. */
    public static List<LivingItemEntity> collectAll(PlayerEntity owner) {
        if (owner == null || owner.getWorld().isClient) return List.of();
        var serverLevel = (ServerWorld) owner.getWorld();
        return serverLevel.getEntitiesByClass(LivingItemEntity.class, owner.getBoundingBox().expand(64.0),
                entity -> owner.getUuid().equals(entity.getOwnerUuid()));
    }

    private static LivingItemEntity summon(World level, PlayerEntity player, ItemStack stack, int ticks, boolean takeOffhand) {
        if (level == null || level.isClient || player == null || stack == null || stack.isEmpty()) return null;
        if (!(level instanceof ServerWorld serverLevel)) return null;
        var entity = PGCEntities.LIVING_ITEM.get().create(serverLevel);
        if (entity == null) return null;
        entity.refreshPositionAndAngles(player.getX(), player.getY() + 1.5, player.getZ(), player.getYaw(), 0);
        entity.startLiving(player, stack, ticks);
        serverLevel.spawnEntity(entity);
        if (takeOffhand && !player.getOffHandStack().isEmpty()) player.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        return entity;
    }
}
