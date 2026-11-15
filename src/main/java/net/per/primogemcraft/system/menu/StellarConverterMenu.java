package net.per.primogemcraft.system.menu;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCMenus;

public class StellarConverterMenu extends ScreenHandler {
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

    private final PropertyDelegate data;
    private final ScreenHandlerContext access;

    public StellarConverterMenu(int id, PlayerInventory inventory, PacketByteBuf buffer) {
        this(id, inventory, new SimpleInventory(SLOT_COUNT), new ArrayPropertyDelegate(StellarConverterBlockEntity.DATA_COUNT),
                ScreenHandlerContext.create(inventory.player.getWorld(), buffer.readBlockPos()));
    }

    public StellarConverterMenu(int id, PlayerInventory inventory, Inventory container, PropertyDelegate data, ScreenHandlerContext access) {
        super(PGCMenus.STELLAR_CONVERTER.get(), id);
        this.data = data;
        this.access = access;
        for (var index = 0; index < SLOT_COUNT; index++) {
            var slotIndex = index;
            addSlot(new Slot(container, slotIndex, SLOT_X[slotIndex], SLOT_Y[slotIndex]) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return StellarConverterBlockEntity.accepts(slotIndex, stack)
                            && !isSlotDisabled(slotIndex) && container.isValid(slotIndex, stack);
                }
            });
        }
        for (var row = 0; row < INVENTORY_ROWS; row++)
            for (var column = 0; column < INVENTORY_COLUMNS; column++)
                addSlot(new Slot(inventory, column + (row + 1) * INVENTORY_COLUMNS, INVENTORY_X + column * SLOT_PITCH, INVENTORY_Y + row * SLOT_PITCH));
        for (var column = 0; column < INVENTORY_COLUMNS; column++)
            addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT_PITCH, HOTBAR_Y));
        addProperties(data);
    }

    public int charge() {
        return data.get(StellarConverterBlockEntity.DATA_CHARGE);
    }

    public boolean isChannelDisabled(int channel) {
        return data.get(channel == 0 ? StellarConverterBlockEntity.DATA_FIRST_DISABLED
                : StellarConverterBlockEntity.DATA_SECOND_DISABLED) != 0;
    }

    public boolean isSlotDisabled(int slot) {
        return switch (slot) {
            case FIRST_INPUT_SLOT, StellarConverterBlockEntity.FIRST_OUTPUT_SLOT -> isChannelDisabled(0);
            case SECOND_INPUT_SLOT, StellarConverterBlockEntity.SECOND_OUTPUT_SLOT -> isChannelDisabled(1);
            default -> false;
        };
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id < 0 || id > 1 || !canUse(player)) return false;
        var index = id == 0 ? StellarConverterBlockEntity.DATA_FIRST_DISABLED
                : StellarConverterBlockEntity.DATA_SECOND_DISABLED;
        data.set(index, isChannelDisabled(id) ? 0 : 1);
        sendContentUpdates();
        return true;
    }

    public int firstCost() {
        return data.get(StellarConverterBlockEntity.DATA_FIRST_COST);
    }

    public int secondCost() {
        return data.get(StellarConverterBlockEntity.DATA_SECOND_COST);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        var slot = slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        var stack = slot.getStack();
        var result = stack.copy();
        if (index < SLOT_COUNT) {
            if (!insertItem(stack, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (StellarConverterBlockEntity.isFuel(stack)) {
            if (!insertItem(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            var moved = !isChannelDisabled(0) && insertItem(stack, FIRST_INPUT_SLOT, FIRST_INPUT_SLOT + 1, false);
            moved |= !isChannelDisabled(1) && insertItem(stack, SECOND_INPUT_SLOT, SECOND_INPUT_SLOT + 1, false);
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, stack);
        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(access, player, PGCBlocks.STELLAR_CONVERTER.get());
    }
}
