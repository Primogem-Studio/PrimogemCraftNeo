package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;
import net.per.primogemcraft.util.PlayerItems;

import java.util.ArrayList;
import java.util.List;

public final class OtherworldBankbook {
    public static final int CAPACITY = 4096;
    public static final int WITHDRAWAL = 64;
    public static final int MODE_OFF = 0;
    public static final int MODE_PAY = 1;
    public static final int MODE_PICKUP = 2;
    public static final int MODE_BOTH = MODE_PAY | MODE_PICKUP;
    public static final int MODE_COUNT = MODE_BOTH + 1;

    private static final String PAID_KEY = "message.primogemcraft.otherworld_bankbook.paid";
    private static final String RECEIVED_KEY = "message.primogemcraft.otherworld_bankbook.received";

    private OtherworldBankbook() {
    }

    public static boolean isBankbook(ItemStack stack) {
        return stack.isOf(PGCItems.OTHERWORLD_BANKBOOK.get());
    }

    public static boolean isFragment(ItemStack stack) {
        return stack.isOf(PGCItems.COSMIC_FRAGMENT.get());
    }

    public static int stored(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.BANKBOOK_FRAGMENTS, 0);
    }

    public static int mode(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.BANKBOOK_MODE, MODE_OFF);
    }

    public static int room(ItemStack stack) {
        return Math.max(0, CAPACITY - stored(stack));
    }

    public static int cycle(ItemStack stack) {
        var next = (mode(stack) + 1) % MODE_COUNT;
        StackComponents.set(stack, PGCDataComponents.BANKBOOK_MODE, next);
        return next;
    }

    public static boolean pays(ItemStack stack) {
        return (mode(stack) & MODE_PAY) != 0;
    }

    public static boolean picksUp(ItemStack stack) {
        return (mode(stack) & MODE_PICKUP) != 0;
    }

    public static int store(ItemStack stack, int amount) {
        var accepted = net.minecraft.util.math.MathHelper.clamp(amount, 0, room(stack));
        if (accepted <= 0) return 0;
        StackComponents.set(stack, PGCDataComponents.BANKBOOK_FRAGMENTS, stored(stack) + accepted);
        return accepted;
    }

    public static int withdraw(ItemStack stack, int amount) {
        var taken = net.minecraft.util.math.MathHelper.clamp(amount, 0, stored(stack));
        if (taken <= 0) return 0;
        StackComponents.set(stack, PGCDataComponents.BANKBOOK_FRAGMENTS, stored(stack) - taken);
        return taken;
    }

    public static List<ItemStack> held(ServerPlayerEntity player) {
        var stacks = new ArrayList<ItemStack>();
        for (var stack : player.getInventory().main) if (isBankbook(stack)) stacks.add(stack);
        for (var stack : player.getInventory().offHand) if (isBankbook(stack)) stacks.add(stack);
        for (var stack : CuriosIntegration.equipped(player)) if (isBankbook(stack)) stacks.add(stack);
        return stacks;
    }

    public static void deposit(ServerPlayerEntity player, ItemStack bankbook) {
        store(bankbook, takeFragments(player, room(bankbook)));
    }

    public static boolean canPayFragments(ServerPlayerEntity player, int amount) {
        if (amount <= 0) return true;
        if (PlayerItems.count(player, PGCItems.COSMIC_FRAGMENT.get()) >= amount) return true;
        var payable = 0;
        for (var stack : held(player)) if (pays(stack)) payable += stored(stack);
        return payable >= amount;
    }

    public static boolean payFragments(ServerPlayerEntity player, int amount) {
        if (!canPayFragments(player, amount)) return false;
        var remaining = amount - takeFragments(player, amount);
        var owed = remaining;
        for (var stack : held(player)) {
            if (remaining <= 0) break;
            if (!pays(stack)) continue;
            remaining -= withdraw(stack, remaining);
        }
        report(player, PAID_KEY, owed - remaining);
        return remaining <= 0;
    }

    public static int totalStored(ServerPlayerEntity player) {
        var total = 0;
        for (var stack : held(player)) total += stored(stack);
        return total;
    }

    public static void give(ServerPlayerEntity player, ItemStack stack) {
        if (!isFragment(stack)) {
            Curios.give(player, stack);
            return;
        }
        var remaining = stack.getCount() - storeFragments(player, stack.getCount());
        if (remaining > 0) Curios.give(player, stack.copyWithCount(remaining));
    }

    public static int storeFragments(ServerPlayerEntity player, int amount) {
        var remaining = amount;
        for (var stack : held(player)) {
            if (remaining <= 0) break;
            if (!picksUp(stack)) continue;
            remaining -= store(stack, remaining);
        }
        var banked = amount - remaining;
        report(player, RECEIVED_KEY, banked);
        return banked;
    }

    public static void absorbFragments(ServerPlayerEntity player, ItemStack dropped) {
        var accepted = storeFragments(player, dropped.getCount());
        if (accepted > 0) dropped.decrement(accepted);
    }

    private static void report(ServerPlayerEntity player, String key, int amount) {
        if (amount <= 0) return;
        player.sendMessage(Text.translatable(key, amount, totalStored(player)), false);
    }

    private static int takeFragments(ServerPlayerEntity player, int amount) {
        var taken = net.minecraft.util.math.MathHelper.clamp(amount, 0, PlayerItems.count(player, PGCItems.COSMIC_FRAGMENT.get()));
        if (taken <= 0) return 0;
        PlayerItems.take(player, PGCItems.COSMIC_FRAGMENT.get(), taken);
        return taken;
    }
}
