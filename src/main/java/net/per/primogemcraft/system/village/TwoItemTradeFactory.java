package net.per.primogemcraft.system.village;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

/**
 * 1.20.1 的 SellItemFactory 只接受单个 ItemStack；
 * 原 Forge 模组用双 ItemStack 表达「买入 / 卖出」交易，这里用自定义 Factory 等价实现。
 */
public record TwoItemTradeFactory(ItemStack buy, ItemStack sell, int maxUses, int experience, float priceMultiplier)
        implements TradeOffers.Factory {
    @Override
    public TradeOffer create(Entity entity, Random random) {
        return new TradeOffer(buy.copy(), sell.copy(), maxUses, experience, priceMultiplier);
    }
}