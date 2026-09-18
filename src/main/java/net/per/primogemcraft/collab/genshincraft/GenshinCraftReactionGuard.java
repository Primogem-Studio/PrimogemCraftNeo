package net.per.primogemcraft.collab.genshincraft;

import net.hackermdch.genshincraft.capability.GenshinCapabilities;
import net.hackermdch.genshincraft.element.Element.Type;
import net.hackermdch.genshincraft.element.ElementDamageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Keeps GenshinCraft's element system off the one reaction it cannot build. Its factory builds the seven
 * appliable types and throws {@code IllegalArgumentException} for every other type, while
 * {@code Anemo.swirl} hands the Frozen aura a Cryo/Hydro reaction leaves behind straight back to that
 * factory - so an Anemo hit on a frozen target throws out of the damage pipeline, and the hit is lost
 * before its damage is ever applied.
 * <p>
 * Such a hit drops its element instead: {@code apply} is the flag GenshinCraft reads before it reacts, so
 * the hit still lands with its plain damage, it just triggers no reaction.
 */
public final class GenshinCraftReactionGuard {
    private GenshinCraftReactionGuard() {
    }

    static void register() {
        NeoForge.EVENT_BUS.register(GenshinCraftReactionGuard.class);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource() instanceof ElementDamageSource source)) return;
        if (source.element.getType() != Type.Anemo) return;
        var handler = event.getEntity().getCapability(GenshinCapabilities.ELEMENT_ENTITY);
        if (handler == null || !handler.getContext().has(Type.Frozen)) return;
        source.setApply(false);
    }
}
