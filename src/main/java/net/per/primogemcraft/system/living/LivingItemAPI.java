package net.per.primogemcraft.system.living;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
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
    public static LivingItemEntity summon(Player player, int ticks) {
        if (player == null) return null;
        return summon(player.level(), player, player.getOffhandItem(), ticks, true);
    }

    /** Summons a living item entity from the player's offhand, forever when {@code infinite}, otherwise for the default duration. */
    public static LivingItemEntity summon(Player player, boolean infinite) {
        if (player == null) return null;
        return summon(player.level(), player, player.getOffhandItem(), infinite ? -1 : DEFAULT_TICKS, true);
    }

    /** Summons a living item entity from the player's offhand in the given level for {@code ticks} ticks. */
    public static LivingItemEntity summon(Level level, Player player, int ticks) {
        if (player == null) return null;
        return summon(level, player, player.getOffhandItem(), ticks, true);
    }

    /** Summons a living item entity from the player's offhand in the given level, forever when {@code infinite}. */
    public static LivingItemEntity summon(Level level, Player player, boolean infinite) {
        if (player == null) return null;
        return summon(level, player, player.getOffhandItem(), infinite ? -1 : DEFAULT_TICKS, true);
    }

    /** Summons a living item entity of the given stack in the given level for {@code ticks} ticks. */
    public static LivingItemEntity summon(Level level, Player player, ItemStack stack, int ticks) {
        return summon(level, player, stack, ticks, false);
    }

    /** Summons a living item entity of the given stack in the given level, forever when {@code infinite}. */
    public static LivingItemEntity summon(Level level, Player player, ItemStack stack, boolean infinite) {
        return summon(level, player, stack, infinite ? -1 : DEFAULT_TICKS, false);
    }

    /** Summons a living item entity that lasts forever from the player's offhand. */
    public static LivingItemEntity summonInfinite(Player player) {
        if (player == null) return null;
        return summon(player.level(), player, player.getOffhandItem(), -1, true);
    }

    /** Summons a living item entity that lasts forever from the player's offhand in the given level. */
    public static LivingItemEntity summonInfinite(Level level, Player player) {
        if (player == null) return null;
        return summon(level, player, player.getOffhandItem(), -1, true);
    }

    /** Summons a living item entity of the given stack that lasts forever. */
    public static LivingItemEntity summonInfinite(Level level, Player player, ItemStack stack) {
        return summon(level, player, stack, -1, false);
    }

    /** Turns every item of the player's inventory and offhand into a living item entity for {@code ticks} ticks and returns how many were summoned. */
    public static int summonAllFromInventory(Player player, int ticks) {
        if (player == null || player.level().isClientSide) return 0;
        var count = 0;
        var inventory = player.getInventory();
        for (var index = 0; index < inventory.items.size(); index++) {
            var stack = inventory.items.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.level(), player, stack, ticks, false) != null) {
                inventory.setItem(index, ItemStack.EMPTY);
                count++;
            }
        }
        var offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && summon(player.level(), player, offhand, ticks, false) != null) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            count++;
        }
        return count;
    }

    /** Turns every item of the player's main inventory, except the hotbar, into a living item entity and returns how many were summoned. */
    public static int summonMainSlots(Player player, int ticks) {
        if (player == null || player.level().isClientSide) return 0;
        var count = 0;
        var inventory = player.getInventory();
        for (var index = 9; index < inventory.items.size(); index++) {
            var stack = inventory.items.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.level(), player, stack, ticks, false) != null) {
                inventory.setItem(index, ItemStack.EMPTY);
                count++;
            }
        }
        return count;
    }

    /** Turns every item of the player's inventory, including the hotbar, armor slots and offhand, into a living item entity and returns how many were summoned. */
    public static int summonAllItems(Player player, int ticks) {
        if (player == null || player.level().isClientSide) return 0;
        var count = 0;
        var inventory = player.getInventory();
        for (var index = 0; index < inventory.items.size(); index++) {
            var stack = inventory.items.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.level(), player, stack, ticks, false) != null) {
                inventory.setItem(index, ItemStack.EMPTY);
                count++;
            }
        }
        for (var index = 0; index < inventory.armor.size(); index++) {
            var stack = inventory.armor.get(index);
            if (stack.isEmpty()) continue;
            if (summon(player.level(), player, stack, ticks, false) != null) {
                inventory.armor.set(index, ItemStack.EMPTY);
                count++;
            }
        }
        var offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && summon(player.level(), player, offhand, ticks, false) != null) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            count++;
        }
        return count;
    }

    /** Moves every dropped living item of the player back to the player's position and returns how many were moved. */
    public static int recallDrops(Player player) {
        if (player == null || player.level().isClientSide) return 0;
        var serverLevel = (ServerLevel) player.level();
        EntityTypeTest<Entity, LivingItemDrop> test = EntityTypeTest.forClass(LivingItemDrop.class);
        var count = 0;
        for (var drop : serverLevel.getEntities(test, entity -> player.getUUID().equals(entity.getOwnerUuid()))) {
            if (drop.isRemoved() || !drop.isAlive()) continue;
            drop.teleportTo(player.getX(), player.getY() + 0.5, player.getZ());
            count++;
        }
        return count;
    }

    /** Sets the remaining duration of every living item of the owner to {@code ticks} and returns how many were affected. */
    public static int setDurationForAll(Player owner, int ticks) {
        var items = collectAll(owner);
        for (var entity : items) entity.setRemainingTicks(ticks);
        return items.size();
    }

    /** Adds {@code deltaTicks} to the remaining duration of the owner's finite living items and returns how many were affected. */
    public static int addDurationForAll(Player owner, int deltaTicks) {
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
    public static List<LivingItemEntity> collectAll(Player owner) {
        if (owner == null || owner.level().isClientSide) return List.of();
        var serverLevel = (ServerLevel) owner.level();
        EntityTypeTest<Entity, LivingItemEntity> test = EntityTypeTest.forClass(LivingItemEntity.class);
        var items = new ArrayList<LivingItemEntity>();
        serverLevel.getEntities(test, entity -> owner.getUUID().equals(entity.getOwnerUuid()), items);
        return items;
    }

    private static LivingItemEntity summon(Level level, Player player, ItemStack stack, int ticks, boolean takeOffhand) {
        if (level == null || level.isClientSide || player == null || stack == null || stack.isEmpty()) return null;
        if (!(level instanceof ServerLevel serverLevel)) return null;
        var entity = PGCEntities.LIVING_ITEM.get().create(serverLevel);
        if (entity == null) return null;
        entity.moveTo(player.getX(), player.getY() + 1.5, player.getZ(), player.getYRot(), 0);
        entity.startLiving(player, stack, ticks);
        serverLevel.addFreshEntity(entity);
        if (takeOffhand && !player.getOffhandItem().isEmpty()) player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        return entity;
    }
}
