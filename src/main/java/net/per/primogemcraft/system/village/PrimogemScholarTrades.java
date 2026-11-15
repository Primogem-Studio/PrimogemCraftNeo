package net.per.primogemcraft.system.village;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.village.TradeOffers;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCVillagerProfessions;

public final class PrimogemScholarTrades {
    public static void register() {
        var profession = PGCVillagerProfessions.PRIMOGEM_SCHOLAR.get();
        TradeOfferHelper.registerVillagerOffers(profession, 1, factories -> {
            factories.add(new TwoItemTradeFactory(new ItemStack(Items.EMERALD), new ItemStack(PGCItems.PRIMOGEM.get(), 5), 20, 5, 0.03f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.PRIMOGEM.get(), 16), new ItemStack(PGCItems.GENESIS_CRYSTAL.get(), 10), 20, 5, 0.01f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.PRIMOGEM.get()), new ItemStack(Items.EMERALD), 10, 10, 0.05f));
        });
        TradeOfferHelper.registerVillagerOffers(profession, 2, factories -> {
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.ACQUAINT_FATE.get()), new ItemStack(PGCItems.MASTERLESS_STARGLITTER.get(), 5), 5, 5, 0f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.ACQUAINT_FATE.get()), new ItemStack(PGCItems.MASTERLESS_STARDUST.get(), 16), 5, 5, 0f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.COSMIC_BIG_LOTTO.get()), new ItemStack(Blocks.TNT, 9), 10, 5, 0.05f));
        });
        TradeOfferHelper.registerVillagerOffers(profession, 3, factories -> {
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.INTERTWINED_FATE.get()), new ItemStack(PGCItems.MASTERLESS_STARGLITTER.get(), 5), 5, 5, 0f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.DUST_OF_AZOTH.get()), new ItemStack(PGCItems.MASTERLESS_STARDUST.get(), 10), 3, 5, 0.03f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.INTERTWINED_FATE.get()), new ItemStack(PGCItems.MASTERLESS_STARDUST.get(), 16), 5, 5, 0f));
        });
        TradeOfferHelper.registerVillagerOffers(profession, 4, factories -> {
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.PRAISE_OF_HIGH_MORALS.get(), 3), new ItemStack(PGCItems.XIAO_LANTERN_LAUNCHER.get()), 1, 5, 0.01f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.HEROS_WIT.get()), new ItemStack(Items.BOOK), 1, 5, 0.05f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.MORA.get(), 34), new ItemStack(PGCItems.MORA_PILE.get()), 10, 5, 0f));
        });
        TradeOfferHelper.registerVillagerOffers(profession, 5, factories -> {
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.HEROS_WIT_REWARD.get()), new ItemStack(PGCItems.HEROS_WIT.get()), 1, 5, 0.02f));
            factories.add(new TwoItemTradeFactory(new ItemStack(PGCItems.ELEMENTAL_MOLTEN_BEAD_FRAGMENT.get()), new ItemStack(PGCItems.STAR_RAIL_SPECIAL_PASS.get(), 6), 1, 5, 0f));
        });
    }
}