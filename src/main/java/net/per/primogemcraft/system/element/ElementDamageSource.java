package net.per.primogemcraft.system.element;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

/**
 * A damage source that carries the element it was dealt with, so an element reaction system can read the
 * element back through {@link ElementDamage#elementOf(DamageSource)} without knowing where the hit came from.
 */
public final class ElementDamageSource extends DamageSource {
    private final Element element;
    private final ElementDamageOptions options;

    ElementDamageSource(Holder<DamageType> type, Element element, ElementDamageOptions options, Entity directEntity, Entity causingEntity) {
        super(type, directEntity, causingEntity);
        this.element = element;
        this.options = options;
    }

    public Element element() {
        return element;
    }

    public ElementDamageOptions options() {
        return options;
    }
}
