package net.per.primogemcraft.system.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.util.PGCTimer;

public record CurioContext(ServerPlayer player, ItemStack stack, CurioForm form, RandomSource random, int production) {
    public CurioContext {
        production = Math.max(1, production);
    }

    public static CurioContext of(ServerPlayer player, ItemStack stack, CurioForm form) {
        var tagged = Curios.formOf(stack);
        return new CurioContext(player, stack, tagged == null ? form : tagged, player.getRandom(), 1);
    }

    public CurioContext withProduction(int production) {
        return new CurioContext(player, stack, form, random, production);
    }

    public ServerLevel level() {
        return player.serverLevel();
    }

    public void heal(float ratio) {
        player.heal(player.getMaxHealth() * ratio);
    }

    public void damage(int amount) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null || bar.denominator() <= 0) return;
        var advanced = bar.advancedBy(amount);
        stack.set(PGCDataComponents.CUSTOM_BAR.get(), advanced);
        if (advanced.remaining() > 0) return;
        destroy();
    }

    public int remaining() {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        return bar == null ? 0 : bar.remaining();
    }

    public int progress() {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        return bar == null ? 0 : bar.numerator();
    }

    public void progress(int numerator, int denominator) {
        if (denominator <= 0) return;
        stack.set(PGCDataComponents.CUSTOM_BAR.get(), new CustomBar(Mth.clamp(numerator, 0, denominator), denominator, true));
    }

    public void destroy() {
        Curios.broken(player, stack);
        var broken = stack.getItem() instanceof CurioItem curio ? curio.byproduct() : ItemStack.EMPTY;
        if (!broken.isEmpty()) give(broken);
        if (stack.getItem() instanceof CurioItem curio) curio.broken(this);
        stack.shrink(1);
    }

    public void announce(Component message) {
        player.displayClientMessage(message, false);
    }

    public boolean chance(double probability) {
        return random.nextDouble() < probability;
    }

    public boolean ready(String name, int ticks) {
        if (!PGCTimer.isDone(player, name)) return false;
        PGCTimer.set(player, name, ticks);
        return true;
    }

    public void give(ItemStack... stacks) {
        for (var given : stacks)
            for (var index = 0; index < production; index++) OtherworldBankbook.give(player, given);
    }

    public ItemStack reward(ItemStack stack) {
        return stack.copyWithCount(stack.getCount() * production);
    }
}
