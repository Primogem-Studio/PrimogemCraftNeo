package net.per.primogemcraft.system.wish;

import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class WishReports {
    private static final String PERCENT = "%";

    private WishReports() {
    }

    public static Text number(int value, Formatting color) {
        return Text.literal(Integer.toString(value)).formatted(color);
    }

    public static Text number(double value, Formatting color) {
        return Text.literal(formatNumber(value)).formatted(color);
    }

    public static Text percent(double ratio, Formatting color) {
        return Text.literal(formatPercent(ratio) + PERCENT).formatted(color);
    }

    public static Text offhandValue(int value) {
        return Text.translatable("message.primogemcraft.wish.core_offhand_value", number(value, Formatting.AQUA));
    }

    public static Text notOwner() {
        return Text.translatable("message.primogemcraft.wish.not_owner");
    }

    public static List<Text> record(ServerPlayerEntity player, WishPity pity) {
        var colorful = WishRoller.isWearingColorfulSunglasses(player);
        var goldLabel = rarityLabel(colorful ? "colorful" : "gold", Formatting.GRAY);
        var purpleLabel = rarityLabel(colorful ? "gold" : "purple", Formatting.GRAY);
        var pulls = pity.goldCount() + pity.purpleCount() + pity.blueCount();
        var average = pity.goldCount() > 0 ? Math.round((float) pulls / pity.goldCount()) : 0;
        return List.of(
                Text.translatable("message.primogemcraft.wish.record_header", player.getName()),
                Text.translatable("message.primogemcraft.wish.record_time", new Date().toString()),
                Text.translatable("message.primogemcraft.wish.record_blue", number(pity.blueCount(), Formatting.AQUA)),
                Text.translatable("message.primogemcraft.wish.record_rare", purpleLabel,
                        number(pity.purpleCount(), Formatting.LIGHT_PURPLE)),
                Text.translatable("message.primogemcraft.wish.record_rare", goldLabel,
                        number(pity.goldCount(), Formatting.GOLD)),
                Text.translatable("message.primogemcraft.wish.record_pity", purpleLabel,
                        number(WishRoller.PURPLE_PITY_LIMIT + 1 - pity.purplePity(), Formatting.GRAY)),
                Text.translatable("message.primogemcraft.wish.record_pity", goldLabel,
                        number(WishRoller.GOLD_PITY_LIMIT + 1 - pity.goldPity(), Formatting.GRAY)),
                Text.translatable("message.primogemcraft.wish.record_average", number(average, Formatting.YELLOW),
                        rarityLabel(colorful ? "colorful" : "gold", Formatting.YELLOW)));
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

    private static Text rarityLabel(String id, Formatting color) {
        return Text.translatable("wish.primogemcraft.rarity_label." + id).formatted(color);
    }
}
