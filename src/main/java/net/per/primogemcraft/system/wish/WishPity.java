package net.per.primogemcraft.system.wish;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record WishPity(int goldPity, int purplePity, int goldCount, int purpleCount, int blueCount) {
    public static final WishPity EMPTY = new WishPity(0, 0, 0, 0, 0);

    public static final Codec<WishPity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("gold_pity").orElse(0).forGetter(WishPity::goldPity),
            Codec.INT.fieldOf("purple_pity").orElse(0).forGetter(WishPity::purplePity),
            Codec.INT.fieldOf("gold_count").orElse(0).forGetter(WishPity::goldCount),
            Codec.INT.fieldOf("purple_count").orElse(0).forGetter(WishPity::purpleCount),
            Codec.INT.fieldOf("blue_count").orElse(0).forGetter(WishPity::blueCount)
    ).apply(instance, WishPity::new));

    public WishPity count(WishRarity rarity, boolean countsPity) {
        var gold = goldCount + (rarity == WishRarity.GOLD ? 1 : 0);
        var purple = purpleCount + (rarity == WishRarity.PURPLE ? 1 : 0);
        var blue = blueCount + (rarity == WishRarity.BLUE ? 1 : 0);
        if (!countsPity) return new WishPity(goldPity, purplePity, gold, purple, blue);
        return switch (rarity) {
            case GOLD -> new WishPity(0, 0, gold, purple, blue);
            case PURPLE -> new WishPity(goldPity + 1, 0, gold, purple, blue);
            case BLUE -> new WishPity(goldPity + 1, purplePity + 1, gold, purple, blue);
        };
    }
}
