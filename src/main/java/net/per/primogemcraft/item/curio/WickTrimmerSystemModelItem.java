package net.per.primogemcraft.item.curio;

import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioImpact;
import net.per.primogemcraft.system.curio.CurioLoot;

public class WickTrimmerSystemModelItem extends ObliterationWickTrimmerItem {
    private static final String GATE = "curio/wick_trimmer_system_model";
    private static final int GATE_TICKS = 5;
    private static final double FRAGMENT_CHANCE_PER_HARDNESS = 0.01D;

    public WickTrimmerSystemModelItem(Properties properties) {
        super(CurioForm.FUSION, properties);
    }

    @Override
    protected void bonus(CurioContext context, CurioImpact impact, float hardness) {
        if (!context.ready(GATE, GATE_TICKS)) return;
        if (!context.chance(hardness * FRAGMENT_CHANCE_PER_HARDNESS)) return;
        CurioLoot.spawn(impact.level(), impact.position(), new ItemStack(PGCItems.COSMIC_FRAGMENT.get()));
    }
}
