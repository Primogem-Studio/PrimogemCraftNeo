package net.per.primogemcraft.system.element;

/**
 * How an elemental hit behaves towards an element reaction system.
 *
 * @param attachesElement whether the hit leaves its element on the target and may start a reaction
 * @param critical        whether the hit renders as a critical elemental hit
 * @param cooldown        whether the hit shares the reaction cooldown
 * @param knockback       whether the hit knocks the target back
 */
public record ElementDamageOptions(boolean attachesElement, boolean critical, boolean cooldown, boolean knockback) {
    public static final ElementDamageOptions DETACHED = new ElementDamageOptions(false, true, false, false);
    public static final ElementDamageOptions REACTING = new ElementDamageOptions(true, false, true, false);
}
