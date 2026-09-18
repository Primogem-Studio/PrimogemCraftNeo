package net.per.primogemcraft.system.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.item.curio.FootprintsOfFateItem;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCMenus;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.weapon.WeaponAttributes;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WishWeapon;

import java.util.List;

public class GorgeousSmithingTableMenu extends AbstractContainerMenu {
    public static final int FIRST_MATERIAL_SLOT = 0;
    public static final int MATERIAL_SLOT_COUNT = 5;
    public static final int FIRST_INVENTORY_SLOT = MATERIAL_SLOT_COUNT;
    public static final int INVENTORY_SLOT_COUNT = Inventory.INVENTORY_SIZE;
    public static final int FIRST_LINKED_SLOT = FIRST_INVENTORY_SLOT + INVENTORY_SLOT_COUNT;
    public static final int ENHANCE_BUTTON = 0;
    public static final int FILL_BUTTON = 1;
    public static final int SELECT_BUTTON_BASE = 100;
    public static final int BLUE_BUTTON_BASE = 400;
    public static final int BLUE_BUTTON_STRIDE = 64;
    public static final int BLUE_BUTTON_MODE_COUNT = 3;
    public static final int LINK_BUTTON_BASE = BLUE_BUTTON_BASE + BLUE_BUTTON_STRIDE * BLUE_BUTTON_MODE_COUNT;
    public static final int BLUE_PICKUP = 0;
    public static final int BLUE_PICKUP_HALF = 1;
    public static final int BLUE_QUICK_MOVE = 2;
    public static final int NO_SELECTION = -1;

    private static final int HIDDEN_SLOT_X = -10000;
    private static final int HIDDEN_SLOT_Y = -10000;

    private final Player player;
    private final ContainerLevelAccess access;
    private final DataSlot selection = DataSlot.standalone();
    private final List<LinkedContainers.Link> links;
    private final int linkedSlotCount;

    public GorgeousSmithingTableMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, new SimpleContainer(MATERIAL_SLOT_COUNT), ContainerLevelAccess.create(inventory.player.level(), data.readBlockPos()), LinkedContainers.read(data));
    }

    public GorgeousSmithingTableMenu(int id, Inventory inventory, Container container, ContainerLevelAccess access, List<LinkedContainers.Link> links) {
        super(PGCMenus.GORGEOUS_SMITHING_TABLE.get(), id);
        player = inventory.player;
        this.access = access;
        this.links = links;
        for (var index = 0; index < MATERIAL_SLOT_COUNT; index++)
            addSlot(materialSlot(container, index, HIDDEN_SLOT_X - index));
        for (var index = 0; index < INVENTORY_SLOT_COUNT; index++)
            addSlot(new Slot(inventory, index, HIDDEN_SLOT_X - MATERIAL_SLOT_COUNT - index, HIDDEN_SLOT_Y));
        var side = player.level().isClientSide();
        var ordinal = 0;
        for (var link : links) {
            var linked = side ? new SimpleContainer(link.size()) : new LinkedContainerView(player.level(), link);
            for (var index = 0; index < link.size(); index++) {
                addSlot(linkedSlot(linked, index, HIDDEN_SLOT_X - FIRST_LINKED_SLOT - ordinal));
                ordinal++;
            }
        }
        linkedSlotCount = ordinal;
        selection.set(NO_SELECTION);
        addDataSlot(selection);
    }

    public int linkedSlotCount() {
        return linkedSlotCount;
    }

    public void writeLinks(FriendlyByteBuf buffer) {
        LinkedContainers.write(buffer, links);
    }

    public ItemStack selectedWeapon() {
        var index = selection.get();
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return ItemStack.EMPTY;
        var stack = player.getInventory().getItem(index);
        return stack.getItem() instanceof WishWeapon ? stack : ItemStack.EMPTY;
    }

    public int selectedIndex() {
        return selection.get();
    }

    public boolean acceptsMaterial(ItemStack stack) {
        return WeaponEnhancement.isEnhancementMaterial(stack, selectedWeapon());
    }

    public boolean canEnhance() {
        if (!(selectedWeapon().getItem() instanceof WishWeapon)) return false;
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++)
            if (slots.get(index).hasItem()) return true;
        return false;
    }

    public boolean canFill() {
        for (var index = 0; index < INVENTORY_SLOT_COUNT; index++) {
            if (index == selection.get()) continue;
            if (autoFillable(player.getInventory().getItem(index))) return true;
        }
        return false;
    }

    public boolean hasInheritedMaterial() {
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++)
            if (WeaponEnhancement.isEnhancedWeapon(slots.get(index).getItem())) return true;
        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == ENHANCE_BUTTON) return enhance();
        if (id == FILL_BUTTON) return fill();
        if (id >= SELECT_BUTTON_BASE && id < SELECT_BUTTON_BASE + INVENTORY_SLOT_COUNT) return select(id - SELECT_BUTTON_BASE);
        if (id >= LINK_BUTTON_BASE) return pullLink(id - LINK_BUTTON_BASE);
        if (id >= BLUE_BUTTON_BASE) {
            var offset = id - BLUE_BUTTON_BASE;
            return blueClick(offset % BLUE_BUTTON_STRIDE, offset / BLUE_BUTTON_STRIDE);
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= FIRST_LINKED_SLOT) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        var stack = slot.getItem();
        var result = stack.copy();
        var moved = index < FIRST_INVENTORY_SLOT
                ? moveItemStackTo(stack, FIRST_INVENTORY_SLOT, FIRST_LINKED_SLOT, true)
                : moveItemStackTo(stack, FIRST_MATERIAL_SLOT, MATERIAL_SLOT_COUNT, false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) return;
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++) {
            var slot = slots.get(index);
            if (!slot.hasItem()) continue;
            var stack = slot.getItem();
            slot.set(ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, PGCBlocks.GORGEOUS_SMITHING_TABLE.get());
    }

    private boolean enhance() {
        var weapon = selectedWeapon();
        if (!(weapon.getItem() instanceof WishWeapon)) return false;
        var changed = false;
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++) {
            var slot = slots.get(index);
            var material = slot.getItem();
            if (WeaponEnhancement.isEnhancedWeapon(material)) {
                for (var refund : WeaponEnhancement.inherit(weapon, material))
                    player.getInventory().placeItemBackInInventory(refund);
                slot.set(ItemStack.EMPTY);
                changed = true;
                continue;
            }
            if (!WeaponEnhancement.refine(weapon, material)) continue;
            slot.set(ItemStack.EMPTY);
            changed = true;
        }
        var discount = footprintDiscount();
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++) {
            if (WeaponEnhancement.enhance(weapon, slots.get(index).getItem(), discount) <= 0) continue;
            changed = true;
        }
        if (!changed) return false;
        WeaponAttributes.refreshLevel(weapon, player);
        WeaponAttributes.refreshPassive(weapon, player, selection.get());
        broadcastChanges();
        return true;
    }

    private boolean fill() {
        var changed = false;
        for (var index = 0; index < INVENTORY_SLOT_COUNT; index++) {
            if (index == selection.get()) continue;
            if (!autoFillable(player.getInventory().getItem(index))) continue;
            if (store(index)) changed = true;
        }
        if (!changed) return false;
        broadcastChanges();
        return true;
    }

    private boolean autoFillable(ItemStack stack) {
        return acceptsMaterial(stack) && !WeaponEnhancement.isEnhancedWeapon(stack);
    }

    private boolean select(int index) {
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return false;
        if (!(player.getInventory().getItem(index).getItem() instanceof WishWeapon)) return false;
        selection.set(selection.get() == index ? NO_SELECTION : index);
        broadcastChanges();
        return true;
    }

    private boolean blueClick(int index, int mode) {
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return false;
        if (index == selection.get()) return false;
        if (mode == BLUE_QUICK_MOVE) {
            if (!store(index)) return false;
            broadcastChanges();
            return true;
        }

        var inventory = player.getInventory();
        var carried = getCarried();
        var source = inventory.getItem(index);
        if (carried.isEmpty()) {
            if (source.isEmpty() || !acceptsMaterial(source)) return false;
            var amount = mode == BLUE_PICKUP_HALF ? Math.max(1, (source.getCount() + 1) / 2) : source.getCount();
            setCarried(source.split(amount));
        } else {
            if (!acceptsMaterial(carried)) return false;
            if (source.isEmpty() || ItemStack.isSameItemSameComponents(source, carried)) {
                var room = source.isEmpty() ? carried.getMaxStackSize() : source.getMaxStackSize() - source.getCount();
                var wanted = mode == BLUE_PICKUP_HALF ? 1 : carried.getCount();
                var moved = Math.min(wanted, room);
                if (moved <= 0) return false;
                if (source.isEmpty()) {
                    inventory.setItem(index, carried.split(moved));
                    setCarried(carried);
                } else {
                    source.grow(moved);
                    carried.shrink(moved);
                    setCarried(carried);
                }
            } else if (mode == BLUE_PICKUP) {
                inventory.setItem(index, carried.copy());
                setCarried(source.copy());
            } else {
                return false;
            }
        }
        inventory.setChanged();
        broadcastChanges();
        return true;
    }

    private boolean store(int index) {
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return false;
        if (index == selection.get()) return false;
        if (!acceptsMaterial(player.getInventory().getItem(index))) return false;
        return pullInto(player.getInventory(), index) > 0;
    }

    private boolean pullLink(int ordinal) {
        if (ordinal < 0 || ordinal >= linkedSlotCount) return false;
        var remaining = ordinal;
        for (var link : links) {
            if (remaining < link.size()) return pull(link, remaining);
            remaining -= link.size();
        }
        return false;
    }

    private boolean pull(LinkedContainers.Link link, int index) {
        var container = LinkedContainers.resolve(player.level(), link.pos());
        if (container == null || index < 0 || index >= container.getContainerSize()) return false;
        var available = container.getItem(index);
        if (available.isEmpty() || !acceptsMaterial(available)) return false;
        if (pullInto(container, index) <= 0) return false;
        broadcastChanges();
        return true;
    }

    private int pullInto(Container source, int index) {
        var moved = 0;
        for (var offset = 0; offset < MATERIAL_SLOT_COUNT; offset++) {
            var available = source.getItem(index);
            if (available.isEmpty()) break;
            var target = slots.get(FIRST_MATERIAL_SLOT + offset);
            var stored = target.getItem();
            if (!stored.isEmpty() && !ItemStack.isSameItemSameComponents(stored, available)) continue;
            var room = stored.isEmpty() ? available.getMaxStackSize() : stored.getMaxStackSize() - stored.getCount();
            if (room <= 0) continue;
            var taken = source.removeItem(index, room);
            if (taken.isEmpty()) break;
            moved += taken.getCount();
            if (stored.isEmpty()) target.set(taken);
            else {
                stored.grow(taken.getCount());
                target.setChanged();
            }
        }
        if (moved > 0) source.setChanged();
        return moved;
    }

    private double footprintDiscount() {
        if (!hasOre()) return 0.0D;
        if (!(player instanceof ServerPlayer server)) return 0.0D;
        var random = server.getRandom();
        for (var context : Curios.held(server)) {
            if (!(context.stack().getItem() instanceof FootprintsOfFateItem)) continue;
            var roll = FootprintsOfFateItem.ensure(context.stack(), random, server.isCreative());
            if (random.nextDouble() >= roll.chance()) continue;
            context.damage(FootprintsOfFateItem.USES_PER_TRIGGER);
            return FootprintsOfFateItem.discount(roll, random);
        }
        return 0.0D;
    }

    private boolean hasOre() {
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++)
            if (WeaponEnhancement.isEnhancementOre(slots.get(index).getItem())) return true;
        return false;
    }

    private Slot materialSlot(Container container, int index, int x) {
        return new Slot(container, index, x, GorgeousSmithingTableMenu.HIDDEN_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return acceptsMaterial(stack);
            }
        };
    }

    private Slot linkedSlot(Container container, int index, int x) {
        return new Slot(container, index, x, HIDDEN_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        };
    }
}
