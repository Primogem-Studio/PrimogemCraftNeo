package net.per.primogemcraft.system.element;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;

/**
 * The single entry point for every elemental hit this mod deals. A source built here always carries its
 * element, whether or not GenshinCraft is installed: without it the hit uses {@link ElementDamageSource},
 * with it the hit becomes GenshinCraft's own elemental damage source, and either way
 * {@link #elementOf(DamageSource)} reads the element back.
 */
public final class ElementDamage {
    private ElementDamage() {
    }

    public static DamageSource of(Element element, Holder<DamageType> type, Entity direct, Entity causing) {
        return of(element, type, direct, causing, ElementStyle.NORMAL, null);
    }

    public static DamageSource of(Element element, Holder<DamageType> type, Entity direct, Entity causing, ElementStyle style, ElementDamageOptions options) {
        return GenshinCraftIntegration.replace(new ElementDamageSource(type, element, options, direct, causing), element, style, options);
    }

    public static DamageSource of(Element element, DamageSource origin) {
        return of(element, origin, ElementStyle.NORMAL, null);
    }

    public static DamageSource of(Element element, DamageSource origin, ElementStyle style, ElementDamageOptions options) {
        return of(element, origin.typeHolder(), origin.getDirectEntity(), origin.getEntity(), style, options);
    }

    /**
     * Reads the element of a hit, or {@code null} when the source carries no element.
     */
    public static Element elementOf(DamageSource source) {
        if (source instanceof ElementDamageSource element) return element.element();
        return GenshinCraftIntegration.elementOf(source);
    }
}
