package net.per.primogemcraft.system.wish;

public record WishResult(WishBanner banner, WishRarity rarity, boolean capturingRadiance, boolean colorful, boolean sunglasses) {
    public WishResult(WishBanner banner, WishRarity rarity, boolean capturingRadiance, boolean colorful) {
        this(banner, rarity, capturingRadiance, colorful, false);
    }
}
