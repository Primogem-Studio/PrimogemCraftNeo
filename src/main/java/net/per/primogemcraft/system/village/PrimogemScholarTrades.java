package net.per.primogemcraft.system.village;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCVillagerProfessions;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class PrimogemScholarTrades {
    @SubscribeEvent
    private static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() != PGCVillagerProfessions.PRIMOGEM_SCHOLAR.get()) return;
        var trades = event.getTrades();
        trades.get(1).add(new BasicItemListing(new ItemStack(PGCItems.PRIMOGEM.get(), 5), new ItemStack(Items.EMERALD), 20, 5, 0.03f));
        trades.get(1).add(new BasicItemListing(new ItemStack(PGCItems.GENESIS_CRYSTAL.get(), 10), new ItemStack(PGCItems.PRIMOGEM.get(), 16), 20, 5, 0.01f));
        trades.get(1).add(new BasicItemListing(new ItemStack(Items.EMERALD), new ItemStack(PGCItems.PRIMOGEM.get()), 10, 10, 0.05f));
        trades.get(2).add(new BasicItemListing(new ItemStack(PGCItems.MASTERLESS_STARGLITTER.get(), 5), new ItemStack(PGCItems.ACQUAINT_FATE.get()), 5, 5, 0f));
        trades.get(2).add(new BasicItemListing(new ItemStack(PGCItems.MASTERLESS_STARDUST.get(), 16), new ItemStack(PGCItems.ACQUAINT_FATE.get()), 5, 5, 0f));
        trades.get(2)
                .add(new BasicItemListing(new ItemStack(Blocks.TNT, 9), new ItemStack(PGCItems.COSMIC_BIG_LOTTO.get()), new ItemStack(PGCItems.UNIDENTIFIED_DOLL.get()), 10, 5, 0.05f));
        trades.get(3).add(new BasicItemListing(new ItemStack(PGCItems.MASTERLESS_STARGLITTER.get(), 5), new ItemStack(PGCItems.INTERTWINED_FATE.get()), 5, 5, 0f));
        trades.get(3).add(new BasicItemListing(new ItemStack(PGCItems.MASTERLESS_STARDUST.get(), 10), new ItemStack(PGCItems.DUST_OF_AZOTH.get()), 3, 5, 0.03f));
        trades.get(3).add(new BasicItemListing(new ItemStack(PGCItems.MASTERLESS_STARDUST.get(), 16), new ItemStack(PGCItems.INTERTWINED_FATE.get()), 5, 5, 0f));
        trades.get(4).add(new BasicItemListing(new ItemStack(PGCItems.XIAO_LANTERN_LAUNCHER.get()), new ItemStack(PGCItems.PRAISE_OF_HIGH_MORALS.get(), 3),
                new ItemStack(PGCItems.CREATIVE_XIAO_LANTERN_LAUNCHER.get()), 1, 5, 0.01f));
        trades.get(4).add(new BasicItemListing(new ItemStack(Items.BOOK), new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(PGCItems.HEROS_WIT.get()), 1, 5, 0.05f));
        trades.get(4).add(new BasicItemListing(new ItemStack(PGCItems.MORA_PILE.get()), new ItemStack(PGCItems.MORA.get(), 34), new ItemStack(PGCItems.PACKED_MORA_PILE.get()), 10, 5, 0f));
        trades.get(5).add(new BasicItemListing(new ItemStack(PGCItems.HEROS_WIT.get()), new ItemStack(Items.EXPERIENCE_BOTTLE, 8), new ItemStack(PGCItems.HEROS_WIT_REWARD.get()), 1, 5, 0.02f));
        trades.get(5).add(new BasicItemListing(new ItemStack(PGCItems.STAR_RAIL_SPECIAL_PASS.get(), 6), new ItemStack(PGCItems.ELEMENTAL_MOLTEN_BEAD_FRAGMENT.get()),
                new ItemStack(PGCItems.ELEMENTAL_MOLTEN_BEAD_FRAGMENT.get(), 2), 1, 5, 0f));
    }
}
