package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

public class SpaceTimePrismItem extends CurioItem {
    private static final int REFINEMENT_BONUS = 1;

    public SpaceTimePrismItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties.fireResistant());
    }

    @Override
    public int refinementBonus() {
        return REFINEMENT_BONUS;
    }
}
