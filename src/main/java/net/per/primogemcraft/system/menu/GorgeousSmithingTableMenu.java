package net.per.primogemcraft.system.menu;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.Property;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.item.curio.FootprintsOfFateItem;
import net.per.primogemcraft.registry.PGCBlocks;
import net.per.primogemcraft.registry.PGCMenus;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.weapon.WeaponAttributes;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WishWeapon;

import java.util.List;

public class GorgeousSmithingTableMenu extends ScreenHandler {
    public static final int FIRST_MATERIAL_SLOT = 0;
    public static final int MATERIAL_SLOT_COUNT = 5;
    public static final int FIRST_INVENTORY_SLOT = MATERIAL_SLOT_COUNT;
    public static final int INVENTORY_SLOT_COUNT = PlayerInventory.MAIN_SIZE;
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

    private final PlayerEntity player;
    private final ScreenHandlerContext access;
    private final Property selection = Property.create();
    private final List<LinkedContainers.Link> links;
    private final int linkedSlotCount;

    public GorgeousSmithingTableMenu(int id, PlayerInventory inventory, PacketByteBuf data) {
        this(id, inventory, new SimpleInventory(MATERIAL_SLOT_COUNT), ScreenHandlerContext.create(inventory.player.getWorld(), data.readBlockPos()), LinkedContainers.read(data));
    }

    public GorgeousSmithingTableMenu(int id, PlayerInventory inventory, Inventory container, ScreenHandlerContext access, List<LinkedContainers.Link> links) {
        super(PGCMenus.GORGEOUS_SMITHING_TABLE.get(), id);
        player = inventory.player;
        this.access = access;
        this.links = links;
        for (var index = 0; index < MATERIAL_SLOT_COUNT; index++)
            addSlot(materialSlot(container, index, HIDDEN_SLOT_X - index));
        for (var index = 0; index < INVENTORY_SLOT_COUNT; index++)
            addSlot(new Slot(inventory, index, HIDDEN_SLOT_X - MATERIAL_SLOT_COUNT - index, HIDDEN_SLOT_Y));
        var side = player.getWorld().isClient();
        var ordinal = 0;
        for (var link : links) {
            var linked = side ? new SimpleInventory(link.size()) : new LinkedContainerView(player.getWorld(), link);
            for (var index = 0; index < link.size(); index++) {
                addSlot(linkedSlot(linked, index, HIDDEN_SLOT_X - FIRST_LINKED_SLOT - ordinal));
                ordinal++;
            }
        }
        linkedSlotCount = ordinal;
        selection.set(NO_SELECTION);
        addProperty(selection);
    }

    public int linkedSlotCount() {
        return linkedSlotCount;
    }

    public void writeLinks(PacketByteBuf buffer) {
        LinkedContainers.write(buffer, links);
    }

    public ItemStack selectedWeapon() {
        var index = selection.get();
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return ItemStack.EMPTY;
        var stack = player.getInventory().getStack(index);
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
            if (slots.get(index).hasStack()) return true;
        return false;
    }

    public boolean canFill() {
        for (var index = 0; index < INVENTORY_SLOT_COUNT; index++) {
            if (index == selection.get()) continue;
            if (autoFillable(player.getInventory().getStack(index))) return true;
        }
        return false;
    }

    public boolean hasInheritedMaterial() {
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++)
            if (WeaponEnhancement.isEnhancedWeapon(slots.get(index).getStack())) return true;
        return false;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
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
    public ItemStack quickMove(PlayerEntity player, int index) {
        if (index < 0 || index >= FIRST_LINKED_SLOT) return ItemStack.EMPTY;
        var slot = slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        var stack = slot.getStack();
        var result = stack.copy();
        var moved = index < FIRST_INVENTORY_SLOT
                ? insertItem(stack, FIRST_INVENTORY_SLOT, FIRST_LINKED_SLOT, true)
                : insertItem(stack, FIRST_MATERIAL_SLOT, MATERIAL_SLOT_COUNT, false);
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return result;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (player.getWorld().isClient()) return;
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++) {
            var slot = slots.get(index);
            if (!slot.hasStack()) continue;
            var stack = slot.getStack();
            slot.setStack(ItemStack.EMPTY);
            player.getInventory().insertStack(stack);
        }
    }

    public boolean canUse(PlayerEntity player) {
        return canUse(access, player, PGCBlocks.GORGEOUS_SMITHING_TABLE.get());
    }

    private boolean enhance() {
        var weapon = selectedWeapon();
        if (!(weapon.getItem() instanceof WishWeapon)) return false;
        var changed = false;
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++) {
            var slot = slots.get(index);
            var material = slot.getStack();
            if (WeaponEnhancement.isEnhancedWeapon(material)) {
                for (var refund : WeaponEnhancement.inherit(weapon, material))
                    player.getInventory().insertStack(refund);
                slot.setStack(ItemStack.EMPTY);
                changed = true;
                continue;
            }
            if (!WeaponEnhancement.refine(weapon, material)) continue;
            slot.setStack(ItemStack.EMPTY);
            changed = true;
        }
        var discount = footprintDiscount();
        for (var index = FIRST_MATERIAL_SLOT; index < FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT; index++) {
            if (WeaponEnhancement.enhance(weapon, slots.get(index).getStack(), discount) <= 0) continue;
            changed = true;
        }
        if (!changed) return false;
        WeaponAttributes.refreshLevel(weapon, player);
        WeaponAttributes.refreshPassive(weapon, player, selection.get());
        sendContentUpdates();
        return true;
    }

    private boolean fill() {
        var changed = false;
        for (var index = 0; index < INVENTORY_SLOT_COUNT; index++) {
            if (index == selection.get()) continue;
            if (!autoFillable(player.getInventory().getStack(index))) continue;
            if (store(index)) changed = true;
        }
        if (!changed) return false;
        sendContentUpdates();
        return true;
    }

    private boolean autoFillable(ItemStack stack) {
        return acceptsMaterial(stack) && !WeaponEnhancement.isEnhancedWeapon(stack);
    }

    private boolean select(int index) {
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return false;
        if (!(player.getInventory().getStack(index).getItem() instanceof WishWeapon)) return false;
        selection.set(selection.get() == index ? NO_SELECTION : index);
        sendContentUpdates();
        return true;
    }

    private boolean blueClick(int index, int mode) {
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return false;
        if (index == selection.get()) return false;
        if (mode == BLUE_QUICK_MOVE) {
            if (!store(index)) return false;
            sendContentUpdates();
            return true;
        }

        var inventory = player.getInventory();
        var carried = getCursorStack();
        var source = inventory.getStack(index);
        if (carried.isEmpty()) {
            if (source.isEmpty() || !acceptsMaterial(source)) return false;
            var amount = mode == BLUE_PICKUP_HALF ? Math.max(1, (source.getCount() + 1) / 2) : source.getCount();
            setCursorStack(source.split(amount));
        } else {
            if (!acceptsMaterial(carried)) return false;
            if (source.isEmpty() || ItemStack.areItemsEqual(source, carried)) {
                var room = source.isEmpty() ? carried.getMaxCount() : source.getMaxCount() - source.getCount();
                var wanted = mode == BLUE_PICKUP_HALF ? 1 : carried.getCount();
                var moved = Math.min(wanted, room);
                if (moved <= 0) return false;
                if (source.isEmpty()) {
                    inventory.setStack(index, carried.split(moved));
                    setCursorStack(carried);
                } else {
                    source.increment(moved);
                    carried.decrement(moved);
                    setCursorStack(carried);
                }
            } else if (mode == BLUE_PICKUP) {
                inventory.setStack(index, carried.copy());
                setCursorStack(source.copy());
            } else {
                return false;
            }
        }
        inventory.markDirty();
        sendContentUpdates();
        return true;
    }

    private boolean store(int index) {
        if (index < 0 || index >= INVENTORY_SLOT_COUNT) return false;
        if (index == selection.get()) return false;
        if (!acceptsMaterial(player.getInventory().getStack(index))) return false;
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
        var container = LinkedContainers.resolve(player.getWorld(), link.pos());
        if (container == null || index < 0 || index >= container.size()) return false;
        var available = container.getStack(index);
        if (available.isEmpty() || !acceptsMaterial(available)) return false;
        if (pullInto(container, index) <= 0) return false;
        sendContentUpdates();
        return true;
    }

    private int pullInto(Inventory source, int index) {
        var moved = 0;
        for (var offset = 0; offset < MATERIAL_SLOT_COUNT; offset++) {
            var available = source.getStack(index);
            if (available.isEmpty()) break;
            var target = slots.get(FIRST_MATERIAL_SLOT + offset);
            var stored = target.getStack();
            if (!stored.isEmpty() && !ItemStack.areItemsEqual(stored, available)) continue;
            var room = stored.isEmpty() ? available.getMaxCount() : stored.getMaxCount() - stored.getCount();
            if (room <= 0) continue;
            var taken = source.removeStack(index, room);
            if (taken.isEmpty()) break;
            moved += taken.getCount();
            if (stored.isEmpty()) target.setStack(taken);
            else {
                stored.increment(taken.getCount());
                target.markDirty();
            }
        }
        if (moved > 0) source.markDirty();
        return moved;
    }

    private double footprintDiscount() {
        if (!hasOre()) return 0.0D;
        if (!(player instanceof ServerPlayerEntity server)) return 0.0D;
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
            if (WeaponEnhancement.isEnhancementOre(slots.get(index).getStack())) return true;
        return false;
    }

    private Slot materialSlot(Inventory container, int index, int x) {
        return new Slot(container, index, x, GorgeousSmithingTableMenu.HIDDEN_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return acceptsMaterial(stack);
            }
        };
    }

    private Slot linkedSlot(Inventory container, int index, int x) {
        return new Slot(container, index, x, HIDDEN_SLOT_Y) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity player) {
                return false;
            }
        };
    }
}
