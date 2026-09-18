package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

public class FaithBondItem extends CurioItem {
    public FaithBondItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public boolean discountsHertaShop() {
        return true;
    }
}
