package net.per.primogemcraft.system.wish;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class WishReports {
    private static final String PERCENT = "%";

    private WishReports() {
    }

    public static Component number(int value, ChatFormatting color) {
        return Component.literal(Integer.toString(value)).withStyle(color);
    }

    public static Component number(double value, ChatFormatting color) {
        return Component.literal(formatNumber(value)).withStyle(color);
    }

    public static Component percent(double ratio, ChatFormatting color) {
        return Component.literal(formatPercent(ratio) + PERCENT).withStyle(color);
    }

    public static Component offhandValue(int value) {
        return Component.translatable("message.primogemcraft.wish.core_offhand_value", number(value, ChatFormatting.AQUA));
    }

    public static Component notOwner() {
        return Component.translatable("message.primogemcraft.wish.not_owner");
    }

    public static List<Component> record(ServerPlayer player, WishPity pity) {
        var colorful = WishRoller.isWearingColorfulSunglasses(player);
        var goldLabel = rarityLabel(colorful ? "colorful" : "gold", ChatFormatting.GRAY);
        var purpleLabel = rarityLabel(colorful ? "gold" : "purple", ChatFormatting.GRAY);
        var pulls = pity.goldCount() + pity.purpleCount() + pity.blueCount();
        var average = pity.goldCount() > 0 ? Math.round((float) pulls / pity.goldCount()) : 0;
        return List.of(
                Component.translatable("message.primogemcraft.wish.record_header", player.getDisplayName()),
                Component.translatable("message.primogemcraft.wish.record_time", new Date().toString()),
                Component.translatable("message.primogemcraft.wish.record_blue", number(pity.blueCount(), ChatFormatting.AQUA)),
                Component.translatable("message.primogemcraft.wish.record_rare", purpleLabel,
                        number(pity.purpleCount(), ChatFormatting.LIGHT_PURPLE)),
                Component.translatable("message.primogemcraft.wish.record_rare", goldLabel,
                        number(pity.goldCount(), ChatFormatting.GOLD)),
                Component.translatable("message.primogemcraft.wish.record_pity", purpleLabel,
                        number(WishRoller.PURPLE_PITY_LIMIT + 1 - pity.purplePity(), ChatFormatting.GRAY)),
                Component.translatable("message.primogemcraft.wish.record_pity", goldLabel,
                        number(WishRoller.GOLD_PITY_LIMIT + 1 - pity.goldPity(), ChatFormatting.GRAY)),
                Component.translatable("message.primogemcraft.wish.record_average", number(average, ChatFormatting.YELLOW),
                        rarityLabel(colorful ? "colorful" : "gold", ChatFormatting.YELLOW)));
    }

    private static String formatPercent(double ratio) {
        return formatNumber(ratio * 100.0D);
    }

    private static String formatNumber(double value) {
        var rounded = Math.rint(value * 100.0D) / 100.0D;
        if (Math.abs(rounded - Math.rint(rounded)) < 0.005D) return Long.toString(Math.round(rounded));
        var text = String.format(Locale.ROOT, "%.2f", rounded);
        var end = text.length();
        while (end > 0 && text.charAt(end - 1) == '0') end--;
        if (end > 0 && text.charAt(end - 1) == '.') end--;
        return text.substring(0, end);
    }

    private static Component rarityLabel(String id, ChatFormatting color) {
        return Component.translatable("wish.primogemcraft.rarity_label." + id).withStyle(color);
    }
}
