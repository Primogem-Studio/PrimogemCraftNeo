package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.system.curio.*;

public class BugNetItem extends CurioItem {
    private static final int PARASITE_LEVEL = 1;
    private static final int PARASITE_TICKS = 200;

    public BugNetItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (Parasitism.level(context.player()) > 0) return;
        Parasitism.apply(context.player(), PARASITE_LEVEL, PARASITE_TICKS);
    }
}
