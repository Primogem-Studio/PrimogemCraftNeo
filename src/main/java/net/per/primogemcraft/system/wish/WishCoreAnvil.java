package net.per.primogemcraft.system.wish;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class WishCoreAnvil {
    public static final int FATE_COUNT = 10;
    public static final int LEVEL_COST = 0;

    @SubscribeEvent
    public static void update(AnvilUpdateEvent event) {
        var left = event.getLeft();
        var right = event.getRight();
        var coreOnLeft = left.is(PGCItems.TEN_PULL_WISH_CORE.get());
        var core = coreOnLeft ? left : right;
        var fates = coreOnLeft ? right : left;
        if (!core.is(PGCItems.TEN_PULL_WISH_CORE.get()) || !fates.is(PGCItems.INTERTWINED_FATE.get())) return;
        var result = assemble(fates, core);
        if (result.isEmpty()) {
            event.setCanceled(true);
            return;
        }
        event.setOutput(result);
        event.setCost(LEVEL_COST);
        event.setMaterialCost(coreOnLeft ? FATE_COUNT : 1);
        if (event.getPlayer().containerMenu instanceof AnvilMenu menu
                && !(menu.getSlot(2) instanceof FreeResultSlot)) {
            var slot = new FreeResultSlot(menu);
            slot.index = 2;
            menu.slots.set(2, slot);
        }
    }

    public static ItemStack assemble(ItemStack fates, ItemStack core) {
        if (!core.is(PGCItems.TEN_PULL_WISH_CORE.get()) || core.getCount() != 1
                || !fates.is(PGCItems.INTERTWINED_FATE.get()) || fates.getCount() != FATE_COUNT
                || WishValue.get(fates) != 0 || fates.has(PGCDataComponents.WISH_VALUE_DIVISOR)
                || WishValue.get(core) <= 0) return ItemStack.EMPTY;
        var result = fates.copy();
        result.set(PGCDataComponents.WISH_VALUE, WishValue.get(core));
        result.set(PGCDataComponents.WISH_VALUE_DIVISOR, FATE_COUNT);
        return result;
    }

    public static ItemStack exampleCore() {
        var core = new ItemStack(PGCItems.TEN_PULL_WISH_CORE.get());
        core.set(PGCDataComponents.WISH_VALUE, 300);
        return core;
    }

    private static final class FreeResultSlot extends Slot {
        private final AnvilMenu menu;
        private final Slot original;

        private FreeResultSlot(AnvilMenu menu) {
            super(menu.getSlot(2).container, menu.getSlot(2).getSlotIndex(), menu.getSlot(2).x, menu.getSlot(2).y);
            this.menu = menu;
            original = menu.getSlot(2);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return original.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            var left = menu.getSlot(0).getItem();
            var right = menu.getSlot(1).getItem();
            var expected = left.is(PGCItems.TEN_PULL_WISH_CORE.get()) ? assemble(right, left) : assemble(left, right);
            return menu.getCost() == 0 && !expected.isEmpty() && ItemStack.matches(expected, getItem())
                    || original.mayPickup(player);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            original.onTake(player, stack);
        }

        @Override
        public void onQuickCraft(ItemStack stack, ItemStack originalStack) {
            original.onQuickCraft(stack, originalStack);
        }
    }
}
