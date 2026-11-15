package net.per.primogemcraft.system.menu;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.compat.component.ItemContainerContents;
import net.per.primogemcraft.registry.PGCDataComponents;

/**
 * A container whose contents live in the item stack that holds it, so a pouch keeps what was put in it while the item
 * travels through inventories. The stack is the live one held by the opener, so every write is persisted at once.
 */
public final class PouchContainer implements Inventory {
    private final ItemStack pouch;
    private final DefaultedList<ItemStack> items;

    public PouchContainer(ItemStack pouch, int capacity) {
        this.pouch = pouch;
        items = DefaultedList.ofSize(capacity, ItemStack.EMPTY);
        var contents = StackComponents.get(pouch, PGCDataComponents.POUCH_CONTENTS);
        if (contents == null) return;
        var stored = Math.min(capacity, contents.getSlots());
        for (var index = 0; index < stored; index++) items.set(index, contents.getStackInSlot(index));
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (var stack : items) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStack(int index) {
        return items.get(index);
    }

    @Override
    public ItemStack removeStack(int index, int amount) {
        var removed = Inventories.splitStack(items, index, amount);
        if (!removed.isEmpty()) markDirty();
        return removed;
    }

    @Override
    public ItemStack removeStack(int index) {
        return Inventories.removeStack(items, index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        items.set(index, stack);
        markDirty();
    }

    @Override
    public void markDirty() {
        StackComponents.set(pouch, PGCDataComponents.POUCH_CONTENTS, ItemContainerContents.fromItems(items));
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    public boolean canInsert(int index, ItemStack stack, net.minecraft.util.math.Direction dir) {
        return !stack.isOf(pouch.getItem());
    }

    @Override
    public void clear() {
        items.clear();
        markDirty();
    }
}
