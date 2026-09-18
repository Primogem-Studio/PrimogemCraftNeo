package net.per.primogemcraft.collab.genshincraft;

import net.hackermdch.genshincraft.api.AddTrounceBlossomLootEvent;
import net.hackermdch.genshincraft.api.RegisterEffectRenderEvent;
import net.hackermdch.genshincraft.block.TrounceBlossom;
import net.hackermdch.genshincraft.data.GenshinComponents;
import net.hackermdch.genshincraft.data.PermanentInfusion;
import net.hackermdch.genshincraft.element.Element.Type;
import net.hackermdch.genshincraft.element.ElementDamageSource;
import net.hackermdch.genshincraft.render.EffectRender;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.per.primogemcraft.item.weapon.element.ElementTools;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import static net.hackermdch.genshincraft.element.Element.fromType;

/**
 * The GenshinCraft linkage. Registered on the mod event bus only while GenshinCraft is installed, so nothing
 * in here is ever loaded — and no GenshinCraft type is ever resolved — without it.
 */
public final class GenshinCraftBridge {
    private static final float QUANTITY = 1.0F;
    private static final int RENDER_LEVEL = 255;
    private static final int ATTACK_BOOST_RENDER_LEVEL = 10;
    private static final double SLIVER_CHANCE = 0.8D;
    private static final double FRAGMENT_CHANCE = 0.4D;
    private static final double CHUNK_CHANCE = 0.1D;
    private static final double WEAPON_CHANCE = 0.01D;
    private static final int SLIVER_COUNT = 4;
    private static final int FRAGMENT_COUNT = 2;

    private GenshinCraftBridge() {
    }

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        ElementTools.elements().forEach((item, element) -> event.modify(item,
                builder -> builder.set(GenshinComponents.PERMANENT_INFUSION, new PermanentInfusion(type(element), false, false))));
    }

    @SubscribeEvent
    public static void onRegisterEffectRender(RegisterEffectRenderEvent event) {
        EffectRender.registerRenderEffect(PGCEffects.PARASITE, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.ATTACK_BOOST, ATTACK_BOOST_RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.AT_DEATHS_DOOR, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.ABUNDANCE, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.BURNING_RETALIATION, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.ABSOLUTE_FAILURE_PRESCRIPTION_ZERO, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.ABSOLUTE_FAILURE_PRESCRIPTION_ONE, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.ABSOLUTE_FAILURE_PRESCRIPTION_TWO, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.DEATHBED, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.THE_PAST, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.CUCKOO_CLOCK_TRICK, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.FISSION_CUCKOO_CLOCK, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.BLACK_FOREST_CUCKOO_CLOCK, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.PERPETUAL_CUCKOO_CLOCK, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.PERSISTENT_FREEZE, RENDER_LEVEL);
        EffectRender.registerRenderEffect(PGCEffects.SCORCHING, RENDER_LEVEL);
    }

    @SubscribeEvent
    public static void onAddTrounceBlossomLoot(AddTrounceBlossomLootEvent event) {
        TrounceBlossom.addLoot(new ItemStack(PGCItems.VAJRADA_AMETHYST_SLIVER.get(), SLIVER_COUNT), SLIVER_CHANCE);
        TrounceBlossom.addLoot(new ItemStack(PGCItems.VAJRADA_AMETHYST_FRAGMENT.get(), FRAGMENT_COUNT), FRAGMENT_CHANCE);
        TrounceBlossom.addLoot(new ItemStack(PGCItems.VAJRADA_AMETHYST_CHUNK.get()), CHUNK_CHANCE);
        TrounceBlossom.addLoot(new ItemStack(PGCItems.MISTSPLITTER_REFORGED.get()), WEAPON_CHANCE);
    }

    static DamageSource element(DamageSource origin, Element element, ElementStyle style, ElementDamageOptions options) {
        var source = new ElementDamageSource(origin, fromType(type(element), QUANTITY));
        switch (style) {
            case LUNAR -> source.lunar();
            case STELLAR -> source.stellar();
        }
        if (options == null) return source;
        return source.setApply(options.attachesElement())
                .setCritical(options.critical())
                .setCooldown(options.cooldown())
                .setKnockback(options.knockback());
    }

    static Element elementOf(DamageSource source) {
        return source instanceof ElementDamageSource elementSource ? element(elementSource.element.getType()) : null;
    }

    private static Type type(Element element) {
        return switch (element) {
            case ANEMO -> Type.Anemo;
            case GEO -> Type.Geo;
            case ELECTRO -> Type.Electro;
            case DENDRO -> Type.Dendro;
            case HYDRO -> Type.Hydro;
            case PYRO -> Type.Pyro;
            case CRYO -> Type.Cryo;
        };
    }

    private static Element element(Type type) {
        return switch (type) {
            case Anemo -> Element.ANEMO;
            case Geo -> Element.GEO;
            case Electro -> Element.ELECTRO;
            case Dendro -> Element.DENDRO;
            case Hydro -> Element.HYDRO;
            case Pyro -> Element.PYRO;
            case Cryo -> Element.CRYO;
            default -> null;
        };
    }
}
