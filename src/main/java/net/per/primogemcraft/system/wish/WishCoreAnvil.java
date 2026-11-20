package net.per.primogemcraft.system.wish;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;

/**
 * 十连祈愿核心 + 10 个纠缠之缘 → 带祈愿值的纠缠之缘，铁砧内零等级消耗。
 *
 * <p>Forge 用 {@code AnvilUpdateEvent} 改写输出与消耗；Fabric 无该事件，
 * 由 {@code AnvilScreenHandlerMixin} 在 {@code updateResult} 末尾回调本类。
 * 原事件的 setMaterialCost 无对应接口，改由 {@link FreeResultSlot} 在取走时自行扣减。
 */
public final class WishCoreAnvil {
    public static final int FATE_COUNT = 10;
    public static final int LEVEL_COST = 0;

    private WishCoreAnvil() {
    }

    public static void update(AnvilScreenHandler menu) {
        var left = menu.getSlot(0).getStack();
        var right = menu.getSlot(1).getStack();
        var coreOnLeft = left.isOf(PGCItems.TEN_PULL_WISH_CORE.get());
        var core = coreOnLeft ? left : right;
        var fates = coreOnLeft ? right : left;
        if (!core.isOf(PGCItems.TEN_PULL_WISH_CORE.get()) || !fates.isOf(PGCItems.INTERTWINED_FATE.get())) return;
        var result = assemble(fates, core);
        var output = menu.getSlot(2);
        if (result.isEmpty()) {
            output.setStack(ItemStack.EMPTY);
            return;
        }
        output.setStack(result);
        ((net.per.primogemcraft.mixin.AnvilScreenHandlerAccessor) menu).primogemcraft$getLevelCostProperty().set(LEVEL_COST);
        if (!(output instanceof FreeResultSlot)) {
            var slot = new FreeResultSlot(menu, coreOnLeft);
            slot.id = 2;
            menu.slots.set(2, slot);
        }
    }

    public static ItemStack assemble(ItemStack fates, ItemStack core) {
        if (!core.isOf(PGCItems.TEN_PULL_WISH_CORE.get()) || core.getCount() != 1
                || !fates.isOf(PGCItems.INTERTWINED_FATE.get()) || fates.getCount() != FATE_COUNT
                || WishValue.get(fates) != 0 || StackComponents.has(fates, PGCDataComponents.WISH_VALUE_DIVISOR)
                || WishValue.get(core) <= 0) return ItemStack.EMPTY;
        var result = fates.copy();
        StackComponents.set(result, PGCDataComponents.WISH_VALUE, WishValue.get(core));
        StackComponents.set(result, PGCDataComponents.WISH_VALUE_DIVISOR, FATE_COUNT);
        return result;
    }

    public static ItemStack exampleCore() {
        var core = new ItemStack(PGCItems.TEN_PULL_WISH_CORE.get());
        StackComponents.set(core, PGCDataComponents.WISH_VALUE, 300);
        return core;
    }

    /** 零等级消耗的结果槽：允许取走并在取走时扣除核心与缘。 */
    private static final class FreeResultSlot extends Slot {
        private final AnvilScreenHandler menu;
        private final Slot original;
        private final boolean coreOnLeft;

        private FreeResultSlot(AnvilScreenHandler menu, boolean coreOnLeft) {
            super(menu.getSlot(2).inventory, menu.getSlot(2).getIndex(), menu.getSlot(2).x, menu.getSlot(2).y);
            this.menu = menu;
            this.original = menu.getSlot(2);
            this.coreOnLeft = coreOnLeft;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return original.canInsert(stack);
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            var left = menu.getSlot(0).getStack();
            var right = menu.getSlot(1).getStack();
            var expected = left.isOf(PGCItems.TEN_PULL_WISH_CORE.get()) ? assemble(right, left) : assemble(left, right);
            return menu.getLevelCost() == LEVEL_COST && !expected.isEmpty()
                    && ItemStack.areEqual(expected, getStack())
                    || original.canTakeItems(player);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            var coreSlot = menu.getSlot(coreOnLeft ? 0 : 1);
            var fateSlot = menu.getSlot(coreOnLeft ? 1 : 0);
            coreSlot.takeStack(1);
            fateSlot.takeStack(FATE_COUNT);
            ((net.per.primogemcraft.mixin.AnvilScreenHandlerAccessor) menu).primogemcraft$getLevelCostProperty().set(LEVEL_COST);
        }

        @Override
        public void onQuickTransfer(ItemStack stack, ItemStack originalStack) {
            original.onQuickTransfer(stack, originalStack);
        }
    }
}