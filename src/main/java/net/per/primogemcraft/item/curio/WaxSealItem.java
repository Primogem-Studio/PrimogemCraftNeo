package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.element.Element;

/**
 * A wax seal of one element. The seal carries no behavior of its own: while it is held, the element
 * gear of the matching element reads it through {@link Element#holdsWaxSeal} and scales its own values.
 */
public class WaxSealItem extends CurioItem {
    private final Element element;

    public WaxSealItem(Element element, Properties properties) {
        super(CurioTrigger.PRESENCE, CurioForm.NORMAL, properties.fireResistant());
        this.element = element;
    }

    public Element element() {
        return element;
    }
}
