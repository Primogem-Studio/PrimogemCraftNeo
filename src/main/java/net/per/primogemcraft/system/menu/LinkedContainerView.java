package net.per.primogemcraft.system.menu;

import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * A read-only window onto a container linked to the gorgeous smithing table. Every read resolves the live container at
 * the linked position, so a container that is unloaded, moved or replaced reads as empty, and no write is ever
 * forwarded to it.
 */
public final class LinkedContainerView implements Inventory {
    private static final int MAX_STACK_SIZE = 64;

    private final World level;
    private final LinkedContainers.Link link;

    public LinkedContainerView(World level, LinkedContainers.Link link) {
        this.level = level;
        this.link = link;
    }

    @Override
    public int size() {
        return link.size();
    }

    @Override
    public boolean isEmpty() {
        for (var index = 0; index < link.size(); index++) if (!getStack(index).isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStack(int index) {
        var container = LinkedContainers.resolve(level, link.pos());
        if (container == null || index < 0 || index >= container.size()) return ItemStack.EMPTY;
        return container.getStack(index);
    }

    @Override
    public ItemStack removeStack(int index, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int index, ItemStack stack) {
    }

    @Override
    public void markDirty() {
        var container = LinkedContainers.resolve(level, link.pos());
        if (container != null) container.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public int getMaxCountPerStack() {
        return MAX_STACK_SIZE;
    }

    @Override
    public void clear() {
    }
}
