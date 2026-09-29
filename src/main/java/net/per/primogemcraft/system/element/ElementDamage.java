package net.per.primogemcraft.system.element;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.config.PGCConfig;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * The single entry point for every elemental hit this mod deals. A source built here always carries its
 * element, whether or not GenshinCraft is installed: without it the hit uses {@link ElementDamageSource},
 * with it the hit becomes GenshinCraft's own elemental damage source, and either way
 * {@link #elementOf(DamageSource)} reads the element back.
 */
@EventBusSubscriber(modid = MOD_ID)
public final class ElementDamage {
    private static final Map<DamageSource, Element> SOURCES = Collections.synchronizedMap(new WeakHashMap<>());

    private ElementDamage() {
    }

    public static DamageSource of(Element element, Holder<DamageType> type, Entity direct, Entity causing) {
        return of(element, type, direct, causing, ElementStyle.NORMAL, null);
    }

    public static DamageSource of(Element element, Holder<DamageType> type, Entity direct, Entity causing, ElementStyle style, ElementDamageOptions options) {
        var original = new ElementDamageSource(type, element, options, direct, causing);
        var source = GenshinCraftIntegration.replace(original, element, style, options);
        if (source != original) SOURCES.put(source, element);
        return source;
    }

    public static boolean isExternallyManaged() {
        return GenshinCraftIntegration.managesElementDamage();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide() || !isExternallyManaged()) return;
        var element = SOURCES.get(event.getSource());
        if (element == null) return;
        var multiplier = PGCConfig.elementDamageMultiplier(element).get();
        if (multiplier != 1.0D) event.setAmount((float) Math.min(Float.MAX_VALUE, event.getAmount() * multiplier));
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
