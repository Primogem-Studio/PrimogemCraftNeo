package net.per.primogemcraft.system.wish;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;

import java.util.ArrayList;
import java.util.List;

public final class WishCoreTransfer {
    public static List<ItemStack> plan(AnvilMenu menu) {
        if (!menu.getCarried().isEmpty() || menu.slots.size() != 39) return List.of();
        var items = new ArrayList<ItemStack>();
        for (var slot : menu.slots) items.add(slot.index == 2 ? ItemStack.EMPTY : slot.getItem().copy());
        var core = ItemStack.EMPTY;
        for (var stack : items) {
            if (stack.is(PGCItems.TEN_PULL_WISH_CORE.get()) && WishValue.get(stack) > 0) {
                core = stack.split(1);
                break;
            }
        }
        if (core.isEmpty()) return List.of();
        var fates = ItemStack.EMPTY;
        for (var candidate : items) {
            if (!candidate.is(PGCItems.INTERTWINED_FATE.get()) || WishValue.get(candidate) != 0
                    || candidate.has(PGCDataComponents.WISH_VALUE_DIVISOR)) continue;
            var count = 0;
            for (var stack : items)
                if (ItemStack.isSameItemSameComponents(candidate, stack)) count += stack.getCount();
            if (count >= WishCoreAnvil.FATE_COUNT) {
                fates = candidate.copyWithCount(WishCoreAnvil.FATE_COUNT);
                break;
            }
        }
        if (fates.isEmpty()) return List.of();
        var remaining = WishCoreAnvil.FATE_COUNT;
        for (var stack : items) {
            if (!ItemStack.isSameItemSameComponents(fates, stack)) continue;
            var amount = Math.min(remaining, stack.getCount());
            stack.shrink(amount);
            remaining -= amount;
            if (remaining == 0) break;
        }
        for (var input = 0; input < 2; input++) {
            var remainder = items.get(input);
            for (var index = 3; index < items.size() && !remainder.isEmpty(); index++) {
                var target = items.get(index);
                if (!ItemStack.isSameItemSameComponents(remainder, target)) continue;
                var amount = Math.min(remainder.getCount(), menu.getSlot(index).getMaxStackSize(target) - target.getCount());
                target.grow(amount);
                remainder.shrink(amount);
            }
            for (var index = 3; index < items.size() && !remainder.isEmpty(); index++) {
                if (items.get(index).isEmpty())
                    items.set(index, remainder.split(Math.min(remainder.getCount(), menu.getSlot(index).getMaxStackSize(remainder))));
            }
            if (!remainder.isEmpty()) return List.of();
        }
        items.set(0, fates);
        items.set(1, core);
        return items;
    }

    public static void transfer(ServerPlayer player, int containerId) {
        if (!(player.containerMenu instanceof AnvilMenu menu) || menu.containerId != containerId || !menu.stillValid(player)) return;
        var items = plan(menu);
        if (items.isEmpty()) return;
        for (var index = 0; index < items.size(); index++)
            if (index != 2) menu.getSlot(index).set(items.get(index));
        menu.createResult();
        menu.broadcastChanges();
    }
}
