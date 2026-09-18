package net.per.primogemcraft.system.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.per.primogemcraft.registry.PGCDataComponents;

/**
 * A container whose contents live in the item stack that holds it, so a pouch keeps what was put in it while the item
 * travels through inventories. The stack is the live one held by the opener, so every write is persisted at once.
 */
public final class PouchContainer implements Container {
    private final ItemStack pouch;
    private final NonNullList<ItemStack> items;

    public PouchContainer(ItemStack pouch, int capacity) {
        this.pouch = pouch;
        items = NonNullList.withSize(capacity, ItemStack.EMPTY);
        var contents = pouch.get(PGCDataComponents.POUCH_CONTENTS.get());
        if (contents == null) return;
        var stored = Math.min(capacity, contents.getSlots());
        for (var index = 0; index < stored; index++) items.set(index, contents.getStackInSlot(index));
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (var stack : items) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int amount) {
        var removed = ContainerHelper.removeItem(items, index, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(items, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        items.set(index, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        pouch.set(PGCDataComponents.POUCH_CONTENTS.get(), ItemContainerContents.fromItems(items));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return !stack.is(pouch.getItem());
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }
}
