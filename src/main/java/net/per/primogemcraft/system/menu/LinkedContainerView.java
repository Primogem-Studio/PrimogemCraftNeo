package net.per.primogemcraft.system.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A read-only window onto a container linked to the gorgeous smithing table. Every read resolves the live container at
 * the linked position, so a container that is unloaded, moved or replaced reads as empty, and no write is ever
 * forwarded to it.
 */
public final class LinkedContainerView implements Container {
    private static final int MAX_STACK_SIZE = 64;

    private final Level level;
    private final LinkedContainers.Link link;

    public LinkedContainerView(Level level, LinkedContainers.Link link) {
        this.level = level;
        this.link = link;
    }

    @Override
    public int getContainerSize() {
        return link.size();
    }

    @Override
    public boolean isEmpty() {
        for (var index = 0; index < link.size(); index++) if (!getItem(index).isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        var container = LinkedContainers.resolve(level, link.pos());
        if (container == null || index < 0 || index >= container.getContainerSize()) return ItemStack.EMPTY;
        return container.getItem(index);
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
    }

    @Override
    public void setChanged() {
        var container = LinkedContainers.resolve(level, link.pos());
        if (container != null) container.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK_SIZE;
    }

    @Override
    public void clearContent() {
    }
}
