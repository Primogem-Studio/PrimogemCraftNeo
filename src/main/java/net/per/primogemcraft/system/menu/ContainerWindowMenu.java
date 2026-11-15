package net.per.primogemcraft.system.menu;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Hand;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCMenus;

/**
 * A scrolling window onto any container. The window shows at most
 * {@link ContainerWindowLayout#MAX_VISIBLE_ROWS} rows of nine slots; the window slots are kept off screen and are
 * drawn, hit tested and scrolled by the screen, while the player inventory below keeps its ordinary slots.
 */
public class ContainerWindowMenu extends ScreenHandler {
    public static final byte SOURCE_MAIN_HAND = 0;
    public static final byte SOURCE_OFF_HAND = 1;
    public static final byte SOURCE_BLOCK = 2;

    private static final int INVENTORY_COLUMNS = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final double MAX_DISTANCE_SQR = 64.0D;

    private final Inventory container;
    private final ScreenHandlerContext access;
    private final Hand hand;
    private final ItemStack holding;
    private final ContainerWindowSound closeSound;
    private final int containerSlots;
    private final int visibleRows;

    public ContainerWindowMenu(int id, PlayerInventory inventory, PacketByteBuf buffer) {
        this(id, inventory, clientSource(inventory, buffer));
    }

    public ContainerWindowMenu(int id, PlayerInventory inventory, Inventory container, Hand hand, ItemStack holding,
                               ScreenHandlerContext access, ContainerWindowSound closeSound) {
        super(PGCMenus.CONTAINER_WINDOW.get(), id);
        this.container = container;
        this.hand = hand;
        this.holding = holding;
        this.access = access;
        this.closeSound = closeSound;
        containerSlots = container.size();
        visibleRows = ContainerWindowLayout.visibleRows(containerSlots);
        var inventoryY = ContainerWindowLayout.inventoryY(visibleRows);
        var hotbarY = ContainerWindowLayout.hotbarY(visibleRows);
        for (var index = 0; index < containerSlots; index++) addSlot(windowSlot(container, index));
        for (var row = 0; row < INVENTORY_ROWS; row++)
            for (var column = 0; column < INVENTORY_COLUMNS; column++)
                addSlot(new Slot(inventory, INVENTORY_COLUMNS + column + row * INVENTORY_COLUMNS,
                        ContainerWindowLayout.columnX(column), inventoryY + row * ContainerWindowLayout.SLOT));
        for (var column = 0; column < INVENTORY_COLUMNS; column++)
            addSlot(new Slot(inventory, column, ContainerWindowLayout.columnX(column), hotbarY));
    }

    public Inventory container() {
        return container;
    }

    public int containerSlots() {
        return containerSlots;
    }

    public int visibleRows() {
        return visibleRows;
    }

    public boolean canUse(PlayerEntity player) {
        if (hand != null) return holding == null || player.getStackInHand(hand) == holding;
        return access.get((level, pos) -> level.getBlockEntity(pos) instanceof Inventory
                && player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= MAX_DISTANCE_SQR, false);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        var slot = slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        var stack = slot.getStack();
        var moved = stack.copy();
        if (index < containerSlots) {
            if (!insertItem(stack, containerSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!insertItem(stack, 0, containerSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        if (stack.getCount() == moved.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, stack);
        return moved;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (closeSound == null || player.getWorld().isClient()) return;
        closeSound.play(player.getWorld(), player.getX(), player.getY(), player.getZ(), player.getRandom());
    }

    private ContainerWindowMenu(int id, PlayerInventory inventory, Source source) {
        this(id, inventory, source.container(), source.hand(), source.holding(), source.access(), source.closeSound());
    }

    private static Slot windowSlot(Inventory container, int index) {
        return new Slot(container, index, ContainerWindowLayout.HIDDEN_X - index, ContainerWindowLayout.HIDDEN_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return container.isValid(index, stack);
            }
        };
    }

    private static Source clientSource(PlayerInventory inventory, PacketByteBuf buffer) {
        var source = buffer.readByte();
        var size = buffer.readVarInt();
        if (source == SOURCE_BLOCK) {
            var pos = buffer.readBlockPos();
            return new Source(new SimpleInventory(size), null, null,
                    ScreenHandlerContext.create(inventory.player.getWorld(), pos), null);
        }
        return new Source(new SimpleInventory(size), source == SOURCE_MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND,
                null, ScreenHandlerContext.EMPTY, null);
    }

    private record Source(Inventory container, Hand hand, ItemStack holding, ScreenHandlerContext access,
                          ContainerWindowSound closeSound) {
    }
}
