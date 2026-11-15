package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;

import java.util.List;

public class ExperienceBookItem extends DescribedItem {
    private static final String STORED_SUFFIX = ".tooltip.stored";

    private final int capacity;

    public ExperienceBookItem(Settings properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return TypedActionResult.success(stack, true);
        var bar = bar(stack);
        if (player.isSneaking()) withdraw(serverPlayer, stack, hand, bar);
        else deposit(serverPlayer, stack, hand, bar);
        return TypedActionResult.success(stack, false);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return bar(stack).numerator() >= capacity;
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        super.appendTooltip(stack, level, tooltip, flag);
        var bar = bar(stack);
        tooltip.add(Text.translatable(stack.getTranslationKey() + STORED_SUFFIX, bar.numerator(), bar.denominator()));
    }

    public int capacity() {
        return capacity;
    }

    private void deposit(ServerPlayerEntity player, ItemStack stack, Hand hand, CustomBar bar) {
        if (bar.remaining() <= 0) {
            player.sendMessage(Text.translatable("message.primogemcraft.experience_book.full"), true);
            return;
        }
        var stored = Math.min(player.totalExperience, bar.remaining());
        if (stored <= 0) return;
        player.addExperience(-stored);
        store(stack, bar.numerator() + stored);
        player.swingHand(hand);
        player.sendMessage(Text.translatable("message.primogemcraft.experience_book.deposited"), true);
    }

    private void withdraw(ServerPlayerEntity player, ItemStack stack, Hand hand, CustomBar bar) {
        if (bar.numerator() <= 0) {
            player.sendMessage(Text.translatable("message.primogemcraft.experience_book.empty"), true);
            return;
        }
        player.addExperience(bar.numerator());
        store(stack, 0);
        player.swingHand(hand);
        player.sendMessage(Text.translatable("message.primogemcraft.experience_book.withdrawn"), true);
    }

    private CustomBar bar(ItemStack stack) {
        return StackComponents.getOrDefault(stack, PGCDataComponents.CUSTOM_BAR, new CustomBar(0, capacity, true));
    }

    private void store(ItemStack stack, int numerator) {
        StackComponents.set(stack, PGCDataComponents.CUSTOM_BAR, new CustomBar(numerator, capacity, numerator < capacity));
    }
}
