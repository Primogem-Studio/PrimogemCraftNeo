package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.LotteryCurioItem;

public class CharmonyBigLottoItem extends LotteryCurioItem {
    private static final String MATERIALS_KEY = "message.primogemcraft.curio.charmony_big_lotto.materials_gained";
    private static final double WIN_ODDS = 0.2D;
    private static final double FINE_ORE_CHANCE = 0.7D;
    private static final int MIN_MATERIALS = 1;
    private static final int MAX_MATERIALS = 5;

    public CharmonyBigLottoItem(Properties properties) {
        super(CurioForm.FUSION, WIN_ODDS, context -> {
            var ore = context.chance(FINE_ORE_CHANCE) ? PGCItems.FINE_ENHANCEMENT_ORE.get() : PGCItems.MYSTIC_ENHANCEMENT_ORE.get();
            context.give(new ItemStack(ore, Mth.nextInt(context.random(), MIN_MATERIALS, MAX_MATERIALS)));
            context.announce(Component.translatable(MATERIALS_KEY));
        }, context -> {
        }, properties);
    }
}
