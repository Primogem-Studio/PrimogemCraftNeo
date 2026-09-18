package net.per.primogemcraft.item.curio;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;

public class CharmonyLostPropertyItem extends LostPropertyItem {
    private static final int DESCRIPTION_LINES = 4;
    private static final int MIN_ORE = 32;
    private static final int MAX_ORE = 64;

    public CharmonyLostPropertyItem(Properties properties) {
        super(CurioForm.FUSION, DESCRIPTION_LINES, properties);
    }

    @Override
    protected void grant(CurioContext context) {
        context.give(new ItemStack(PGCItems.FINE_ENHANCEMENT_ORE.get(), Mth.nextInt(context.random(), MIN_ORE, MAX_ORE)));
    }
}
