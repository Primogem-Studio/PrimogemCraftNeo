package net.per.primogemcraft.system.curio;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.util.PGCTimer;

public record CurioContext(ServerPlayerEntity player, ItemStack stack, CurioForm form, Random random, int production) {
    public CurioContext {
        production = Math.max(1, production);
    }

    public static CurioContext of(ServerPlayerEntity player, ItemStack stack, CurioForm form) {
        var tagged = Curios.formOf(stack);
        return new CurioContext(player, stack, tagged == null ? form : tagged, player.getRandom(), 1);
    }

    public CurioContext withProduction(int production) {
        return new CurioContext(player, stack, form, random, production);
    }

    public ServerWorld getWorld() {
        return player.getServerWorld();
    }

    public void heal(float ratio) {
        player.heal(player.getMaxHealth() * ratio);
    }

    public void damage(int amount) {
        var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
        if (bar == null || bar.denominator() <= 0) return;
        var advanced = bar.advancedBy(amount);
        StackComponents.set(stack, PGCDataComponents.CUSTOM_BAR, advanced);
        if (advanced.remaining() > 0) return;
        destroy();
    }

    public int remaining() {
        var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
        return bar == null ? 0 : bar.remaining();
    }

    public int progress() {
        var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
        return bar == null ? 0 : bar.numerator();
    }

    public void progress(int numerator, int denominator) {
        if (denominator <= 0) return;
        StackComponents.set(stack, PGCDataComponents.CUSTOM_BAR, new CustomBar(MathHelper.clamp(numerator, 0, denominator), denominator, true));
    }

    public void destroy() {
        Curios.broken(player, stack);
        var broken = stack.getItem() instanceof CurioItem curio ? curio.byproduct() : ItemStack.EMPTY;
        if (!broken.isEmpty()) give(broken);
        if (stack.getItem() instanceof CurioItem curio) curio.broken(this);
        stack.decrement(1);
    }

    public void announce(Text message) {
        player.sendMessage(message, false);
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
