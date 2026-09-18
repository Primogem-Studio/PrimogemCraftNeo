package net.per.primogemcraft.system.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCMenus;

/**
 * A scrolling window onto any container. The window shows at most
 * {@link ContainerWindowLayout#MAX_VISIBLE_ROWS} rows of nine slots; the window slots are kept off screen and are
 * drawn, hit tested and scrolled by the screen, while the player inventory below keeps its ordinary slots.
 */
public class ContainerWindowMenu extends AbstractContainerMenu {
    public static final byte SOURCE_MAIN_HAND = 0;
    public static final byte SOURCE_OFF_HAND = 1;
    public static final byte SOURCE_BLOCK = 2;

    private static final int INVENTORY_COLUMNS = 9;
    private static final int INVENTORY_ROWS = 3;
    private static final double MAX_DISTANCE_SQR = 64.0D;

    private final Container container;
    private final ContainerLevelAccess access;
    private final InteractionHand hand;
    private final ItemStack holding;
    private final ContainerWindowSound closeSound;
    private final int containerSlots;
    private final int visibleRows;

    public ContainerWindowMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, clientSource(inventory, buffer));
    }

    public ContainerWindowMenu(int id, Inventory inventory, Container container, InteractionHand hand, ItemStack holding,
                               ContainerLevelAccess access, ContainerWindowSound closeSound) {
        super(PGCMenus.CONTAINER_WINDOW.get(), id);
        this.container = container;
        this.hand = hand;
        this.holding = holding;
        this.access = access;
        this.closeSound = closeSound;
        containerSlots = container.getContainerSize();
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

    public Container container() {
        return container;
    }

    public int containerSlots() {
        return containerSlots;
    }

    public int visibleRows() {
        return visibleRows;
    }

    @Override
    public boolean stillValid(Player player) {
        if (hand != null) return holding == null || player.getItemInHand(hand) == holding;
        return access.evaluate((level, pos) -> level.getBlockEntity(pos) instanceof Container
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= MAX_DISTANCE_SQR, false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var moved = stack.copy();
        if (index < containerSlots) {
            if (!moveItemStackTo(stack, containerSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, containerSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == moved.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return moved;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (closeSound == null || player.level().isClientSide()) return;
        closeSound.play(player.level(), player.getX(), player.getY(), player.getZ(), player.getRandom());
    }

    private ContainerWindowMenu(int id, Inventory inventory, Source source) {
        this(id, inventory, source.container(), source.hand(), source.holding(), source.access(), source.closeSound());
    }

    private static Slot windowSlot(Container container, int index) {
        return new Slot(container, index, ContainerWindowLayout.HIDDEN_X - index, ContainerWindowLayout.HIDDEN_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(index, stack);
            }
        };
    }

    private static Source clientSource(Inventory inventory, FriendlyByteBuf buffer) {
        var source = buffer.readByte();
        var size = buffer.readVarInt();
        if (source == SOURCE_BLOCK) {
            var pos = buffer.readBlockPos();
            return new Source(new SimpleContainer(size), null, null,
                    ContainerLevelAccess.create(inventory.player.level(), pos), null);
        }
        return new Source(new SimpleContainer(size), source == SOURCE_MAIN_HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                null, ContainerLevelAccess.NULL, null);
    }

    private record Source(Container container, InteractionHand hand, ItemStack holding, ContainerLevelAccess access,
                          ContainerWindowSound closeSound) {
    }
}
