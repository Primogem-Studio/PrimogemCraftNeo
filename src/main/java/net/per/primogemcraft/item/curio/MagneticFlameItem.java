package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

public class MagneticFlameItem extends CurioItem {
    private static final float BREAK_CHANCE = 0.15F;

    public MagneticFlameItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, 1, properties);
    }

    @Override
    public void presence(CurioContext context) {
        if (!EnigmataMagnetismItem.consumeFlameFlag(context.player())) return;
        if (context.chance(BREAK_CHANCE)) context.damage(1);
    }
}
