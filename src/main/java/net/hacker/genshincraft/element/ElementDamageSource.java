package net.hacker.genshincraft.element;

import net.minecraft.entity.damage.DamageSource;

/**
 * GenshinCraft API stub for compilation. At runtime the real GenshinCraft mod provides this class.
 */
public class ElementDamageSource extends DamageSource {
    public final Element element;

    public ElementDamageSource(DamageSource origin, Element element) {
        super(origin.getTypeRegistryEntry(), origin.getSource(), origin.getAttacker());
        this.element = element;
    }

    public ElementDamageSource setApply(boolean apply) { return this; }
    public ElementDamageSource setCooldown(boolean cooldown) { return this; }
    public ElementDamageSource setKnockback(boolean knockback) { return this; }
}
