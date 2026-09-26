package net.per.primogemcraft.item.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.wish.WishBanner;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishRoller;
import net.per.primogemcraft.system.wish.WishTooltips;
import net.per.primogemcraft.system.wish.WishValue;

import java.util.List;

public class WishCoreItem extends Item {
    public static final int TEN_PULL_CAPACITY = 300;
    private static final float FEED_VOLUME = 0.5F;
    private static final float FEED_PITCH_STEP = 0.05F;
    private final int capacity;

    public WishCoreItem(Properties properties) {
        this(properties, WishValue.CAPACITY);
    }

    public WishCoreItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            feed(serverPlayer, stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (WishTooltips.showsDetails()) {
            var wishValue = WishValue.get(stack);
            var perPull = capacity == TEN_PULL_CAPACITY ? wishValue / 10.0D : wishValue;
            tooltip.add(Component.translatable(tooltipKey(0), WishReports.number(wishValue, ChatFormatting.AQUA)));
            if (wishValue > 0) {
                tooltip.add(Component.translatable(tooltipKey(1), WishReports.percent(WishRoller.purpleChanceBonus(WishBanner.INTERTWINED, perPull), ChatFormatting.LIGHT_PURPLE)));
                tooltip.add(Component.translatable(tooltipKey(2), WishReports.percent(WishRoller.goldChanceBonus(WishBanner.INTERTWINED, perPull), ChatFormatting.GOLD)));
            }
        } else tooltip.add(Component.translatable(tooltipKey(3)));

        if (WishTooltips.showsTutorial()) {
            tooltip.add(Component.translatable(tooltipKey(5)));
            tooltip.add(Component.translatable(tooltipKey(6)));
            tooltip.add(Component.translatable(tooltipKey(7)));
        } else {
            tooltip.add(Component.translatable(tooltipKey(4)));
        }
    }

    private void feed(ServerPlayer player, ItemStack stack) {
        var bar = stack.getOrDefault(PGCDataComponents.CUSTOM_BAR.get(), new CustomBar(0, capacity, true));
        if (bar.remaining() <= 0) {
            player.displayClientMessage(Component.translatable("message.primogemcraft.wish.core_full", WishReports.number(bar.denominator(), ChatFormatting.RED)), true);
            return;
        }

        var consumed = WishValue.absorb(stack, player, bar.remaining(), player.isShiftKeyDown());
        if (consumed <= 0) {
            player.displayClientMessage(Component.translatable("message.primogemcraft.wish.core_no_material"), true);
            return;
        }

        var advanced = bar.advancedBy(consumed);
        stack.set(PGCDataComponents.CUSTOM_BAR.get(), advanced);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.WISH_CORE_FEED.get(), SoundSource.PLAYERS, FEED_VOLUME, advanced.numerator() * FEED_PITCH_STEP);
    }

    private String tooltipKey(int index) {
        return "item.primogemcraft.wish_core.tooltip." + index;
    }
}
