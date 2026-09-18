package net.per.primogemcraft.system.wish;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.per.primogemcraft.config.PGCConfig;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.registry.PGCItems;

import java.util.ArrayList;
import java.util.List;

public final class WishRoller {
    public static final int GOLD_PITY_LIMIT = 49;
    public static final int PURPLE_PITY_LIMIT = 9;

    private static final double BASE_GOLD_CHANCE = 0.03D;
    private static final double GOLD_CHANCE_PER_VALUE = 0.001D;
    private static final double BASE_PURPLE_CHANCE = 0.2D;
    private static final double PURPLE_CHANCE_PER_VALUE = 0.002D;
    private static final double MAX_CHANCE = 0.9D;
    private static final double COLORFUL_SUNGLASSES_GOLD_MULTIPLIER = 2.0D;
    private static final double PERCENT = 100.0D;

    private WishRoller() {
    }

    public static List<WishResult> roll(ServerPlayer player, WishBanner banner, int wishValue, int count) {
        var results = new ArrayList<WishResult>(count);
        for (var index = 0; index < count; index++) results.add(roll(player, banner, wishValue));
        return results;
    }

    public static WishResult roll(ServerPlayer player, WishBanner banner, int wishValue) {
        return roll(player, banner, wishValue, false);
    }

    public static List<WishResult> rollGuaranteedGold(ServerPlayer player, WishBanner banner, int count) {
        var results = new ArrayList<WishResult>(count);
        for (var index = 0; index < count; index++) results.add(roll(player, banner, 0, true));
        return results;
    }

    private static WishResult roll(ServerPlayer player, WishBanner banner, int wishValue, boolean guaranteedGold) {
        var random = player.getRandom();
        var pity = player.getData(PGCAttachments.WISH_PITY.get());
        var colorful = isWearingColorfulSunglasses(player);
        var rarity = guaranteedGold ? WishRarity.GOLD : decide(random, pity, banner, wishValue, colorful);
        player.setData(PGCAttachments.WISH_PITY.get(), pity.count(rarity, banner.countsPity()));
        var capturingRadiance = banner.countsPity() && rarity == WishRarity.GOLD && random.nextDouble() < capturingRadianceChance();
        return new WishResult(banner, rarity, capturingRadiance, colorful);
    }

    public static double capturingRadianceChance() {
        return PGCConfig.CAPTURING_RADIANCE_CHANCE.get() / PERCENT;
    }

    private static WishRarity decide(RandomSource random, WishPity pity, WishBanner banner, int wishValue, boolean colorful) {
        var goldChance = goldChance(banner, wishValue);
        if (colorful) goldChance = Math.min(MAX_CHANCE, goldChance * COLORFUL_SUNGLASSES_GOLD_MULTIPLIER);
        if ((banner.countsPity() && pity.goldPity() >= GOLD_PITY_LIMIT) || random.nextDouble() < goldChance) {
            return WishRarity.GOLD;
        }

        if ((banner.countsPity() && pity.purplePity() >= PURPLE_PITY_LIMIT) || random.nextDouble() < purpleChance(banner, wishValue)) {
            return WishRarity.PURPLE;
        }

        return WishRarity.BLUE;
    }

    public static double goldChance(WishBanner banner, int wishValue) {
        return Math.min(MAX_CHANCE, BASE_GOLD_CHANCE + GOLD_CHANCE_PER_VALUE * countedValue(banner, wishValue));
    }

    public static double purpleChance(WishBanner banner, int wishValue) {
        return Math.min(MAX_CHANCE, BASE_PURPLE_CHANCE + PURPLE_CHANCE_PER_VALUE * countedValue(banner, wishValue));
    }

    public static double goldChanceBonus(WishBanner banner, int wishValue) {
        return goldChance(banner, wishValue) - BASE_GOLD_CHANCE;
    }

    public static double purpleChanceBonus(WishBanner banner, int wishValue) {
        return purpleChance(banner, wishValue) - BASE_PURPLE_CHANCE;
    }

    private static int countedValue(WishBanner banner, int wishValue) {
        return banner.countsPity() ? wishValue : 0;
    }

    public static boolean isWearingColorfulSunglasses(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(PGCItems.COLORFUL_SUNGLASSES.get());
    }
}
