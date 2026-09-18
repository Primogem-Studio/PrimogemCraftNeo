package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCDataComponents;

import java.util.List;

public class ExperienceBookItem extends DescribedItem {
    private static final String STORED_SUFFIX = ".tooltip.stored";

    private final int capacity;

    public ExperienceBookItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.sidedSuccess(stack, true);
        var bar = bar(stack);
        if (player.isShiftKeyDown()) withdraw(serverPlayer, stack, hand, bar);
        else deposit(serverPlayer, stack, hand, bar);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return bar(stack).numerator() >= capacity;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var bar = bar(stack);
        tooltip.add(Component.translatable(stack.getDescriptionId() + STORED_SUFFIX, bar.numerator(), bar.denominator()));
    }

    public int capacity() {
        return capacity;
    }

    private void deposit(ServerPlayer player, ItemStack stack, InteractionHand hand, CustomBar bar) {
        if (bar.remaining() <= 0) {
            player.displayClientMessage(Component.translatable("message.primogemcraft.experience_book.full"), true);
            return;
        }
        var stored = Math.min(player.totalExperience, bar.remaining());
        if (stored <= 0) return;
        player.giveExperiencePoints(-stored);
        store(stack, bar.numerator() + stored);
        player.swing(hand, true);
        player.displayClientMessage(Component.translatable("message.primogemcraft.experience_book.deposited"), true);
    }

    private void withdraw(ServerPlayer player, ItemStack stack, InteractionHand hand, CustomBar bar) {
        if (bar.numerator() <= 0) {
            player.displayClientMessage(Component.translatable("message.primogemcraft.experience_book.empty"), true);
            return;
        }
        player.giveExperiencePoints(bar.numerator());
        store(stack, 0);
        player.swing(hand, true);
        player.displayClientMessage(Component.translatable("message.primogemcraft.experience_book.withdrawn"), true);
    }

    private CustomBar bar(ItemStack stack) {
        return stack.getOrDefault(PGCDataComponents.CUSTOM_BAR.get(), new CustomBar(0, capacity, true));
    }

    private void store(ItemStack stack, int numerator) {
        stack.set(PGCDataComponents.CUSTOM_BAR.get(), new CustomBar(numerator, capacity, numerator < capacity));
    }
}
