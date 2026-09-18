package net.per.primogemcraft.system.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCMenus;

public class StellarConverterMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = StellarConverterBlockEntity.SLOT_COUNT;
    public static final int FUEL_SLOT = StellarConverterBlockEntity.FUEL_SLOT;
    public static final int FIRST_INPUT_SLOT = StellarConverterBlockEntity.FIRST_INPUT_SLOT;
    public static final int SECOND_INPUT_SLOT = StellarConverterBlockEntity.SECOND_INPUT_SLOT;

    private static final int[] SLOT_X = {16, 66, 135, 66, 135};
    private static final int[] SLOT_Y = {61, 23, 23, 50, 50};
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_PITCH = 18;
    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLUMNS = 9;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    public StellarConverterMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(StellarConverterBlockEntity.DATA_COUNT),
                ContainerLevelAccess.create(inventory.player.level(), buffer.readBlockPos()));
    }

    public StellarConverterMenu(int id, Inventory inventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(PGCMenus.STELLAR_CONVERTER.get(), id);
        this.data = data;
        this.access = access;
        for (var index = 0; index < SLOT_COUNT; index++) {
            var slotIndex = index;
            addSlot(new Slot(container, slotIndex, SLOT_X[slotIndex], SLOT_Y[slotIndex]) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return StellarConverterBlockEntity.accepts(slotIndex, stack);
                }
            });
        }
        for (var row = 0; row < INVENTORY_ROWS; row++)
            for (var column = 0; column < INVENTORY_COLUMNS; column++)
                addSlot(new Slot(inventory, column + (row + 1) * INVENTORY_COLUMNS, INVENTORY_X + column * SLOT_PITCH, INVENTORY_Y + row * SLOT_PITCH));
        for (var column = 0; column < INVENTORY_COLUMNS; column++)
            addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT_PITCH, HOTBAR_Y));
        addDataSlots(data);
    }

    public int charge() {
        return data.get(StellarConverterBlockEntity.DATA_CHARGE);
    }

    public int firstCost() {
        return data.get(StellarConverterBlockEntity.DATA_FIRST_COST);
    }

    public int secondCost() {
        return data.get(StellarConverterBlockEntity.DATA_SECOND_COST);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var result = stack.copy();
        if (index < SLOT_COUNT) {
            if (!moveItemStackTo(stack, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (StellarConverterBlockEntity.isFuel(stack)) {
            if (!moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, FIRST_INPUT_SLOT, SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, PGCBlocks.STELLAR_CONVERTER.get());
    }
}
