package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
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
    private static final float FEED_VOLUME = 0.5F;
    private static final float FEED_PITCH_STEP = 0.05F;

    /** 十连祈愿核心的容量上限（单抽为 WishValue.CAPACITY）。 */
    public static final int TEN_PULL_CAPACITY = 300;

    private final int capacity;

    public WishCoreItem(Settings properties) {
        this(properties, WishValue.CAPACITY);
    }

    public WishCoreItem(Settings properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (player instanceof ServerPlayerEntity serverPlayer) {
            feed(serverPlayer, stack);
        }
        return TypedActionResult.consume(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        if (WishTooltips.showsDetails()) {
            var wishValue = WishValue.get(stack);
            tooltip.add(Text.translatable(tooltipKey(0), WishReports.number(wishValue, Formatting.AQUA)));
            if (wishValue > 0) {
                tooltip.add(Text.translatable(tooltipKey(1), WishReports.percent(WishRoller.purpleChanceBonus(WishBanner.INTERTWINED, wishValue), Formatting.LIGHT_PURPLE)));
                tooltip.add(Text.translatable(tooltipKey(2), WishReports.percent(WishRoller.goldChanceBonus(WishBanner.INTERTWINED, wishValue), Formatting.GOLD)));
            }
        } else tooltip.add(Text.translatable(tooltipKey(3)));

        if (WishTooltips.showsTutorial()) {
            tooltip.add(Text.translatable(tooltipKey(5)));
            tooltip.add(Text.translatable(tooltipKey(6)));
            tooltip.add(Text.translatable(tooltipKey(7)));
        } else {
            tooltip.add(Text.translatable(tooltipKey(4)));
        }
    }

    private void feed(ServerPlayerEntity player, ItemStack stack) {
        var bar = StackComponents.getOrDefault(stack, PGCDataComponents.CUSTOM_BAR, new CustomBar(0, capacity, true));
        if (bar.remaining() <= 0) {
            player.sendMessage(Text.translatable("message.primogemcraft.wish.core_full", WishReports.number(bar.denominator(), Formatting.RED)), true);
            return;
        }

        var consumed = WishValue.absorb(stack, player, bar.remaining(), player.isSneaking());
        if (consumed <= 0) {
            player.sendMessage(Text.translatable("message.primogemcraft.wish.core_no_material"), true);
            return;
        }

        var advanced = bar.advancedBy(consumed);
        StackComponents.set(stack, PGCDataComponents.CUSTOM_BAR, advanced);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.WISH_CORE_FEED.get(), SoundCategory.PLAYERS, FEED_VOLUME, advanced.numerator() * FEED_PITCH_STEP);
    }

    private String tooltipKey(int index) {
        return "item.primogemcraft.wish_core.tooltip." + index;
    }
}
