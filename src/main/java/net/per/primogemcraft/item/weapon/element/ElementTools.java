package net.per.primogemcraft.item.weapon.element;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;

import java.util.Map;

import static java.util.Map.entry;
import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * The element every element wish weapon carries. The element goes into the item's default components, so
 * {@link Element#of(net.minecraft.world.item.ItemStack)} reads it from any stack of the tool, and the
 * GenshinCraft linkage hands the same table over as a permanent infusion.
 */
@EventBusSubscriber(modid = MOD_ID)
public final class ElementTools {
    private static final Map<DeferredItem<?>, Element> ELEMENTS = Map.ofEntries(
            entry(PGCItems.VAYUDA_TURQUOISE_PICKAXE, Element.ANEMO),
            entry(PGCItems.VAYUDA_TURQUOISE_AXE, Element.ANEMO),
            entry(PGCItems.VAYUDA_TURQUOISE_HOE, Element.ANEMO),
            entry(PGCItems.VAYUDA_TURQUOISE_SHOVEL, Element.ANEMO),
            entry(PGCItems.PRITHIVA_TOPAZ_PICKAXE, Element.GEO),
            entry(PGCItems.PRITHIVA_TOPAZ_AXE, Element.GEO),
            entry(PGCItems.PRITHIVA_TOPAZ_HOE, Element.GEO),
            entry(PGCItems.PRITHIVA_TOPAZ_SHOVEL, Element.GEO),
            entry(PGCItems.VAJRADA_AMETHYST_PICKAXE, Element.ELECTRO),
            entry(PGCItems.VAJRADA_AMETHYST_AXE, Element.ELECTRO),
            entry(PGCItems.VAJRADA_AMETHYST_HOE, Element.ELECTRO),
            entry(PGCItems.VAJRADA_AMETHYST_SHOVEL, Element.ELECTRO),
            entry(PGCItems.NAGADUS_EMERALD_PICKAXE, Element.DENDRO),
            entry(PGCItems.NAGADUS_EMERALD_AXE, Element.DENDRO),
            entry(PGCItems.NAGADUS_EMERALD_HOE, Element.DENDRO),
            entry(PGCItems.NAGADUS_EMERALD_SHOVEL, Element.DENDRO),
            entry(PGCItems.VARUNADA_LAZURITE_PICKAXE, Element.HYDRO),
            entry(PGCItems.VARUNADA_LAZURITE_AXE, Element.HYDRO),
            entry(PGCItems.VARUNADA_LAZURITE_SHOVEL, Element.HYDRO),
            entry(PGCItems.VARUNADA_LAZURITE_HOE, Element.HYDRO),
            entry(PGCItems.VARUNADA_LAZURITE_BLOSSOM_PICKAXE, Element.HYDRO),
            entry(PGCItems.AGNIDUS_AGATE_PICKAXE, Element.PYRO),
            entry(PGCItems.AGNIDUS_AGATE_AXE, Element.PYRO),
            entry(PGCItems.AGNIDUS_AGATE_HOE, Element.PYRO),
            entry(PGCItems.AGNIDUS_AGATE_SHOVEL, Element.PYRO),
            entry(PGCItems.SHIVADA_JADE_PICKAXE, Element.CRYO),
            entry(PGCItems.SHIVADA_JADE_AXE, Element.CRYO),
            entry(PGCItems.SHIVADA_JADE_HOE, Element.CRYO),
            entry(PGCItems.SHIVADA_JADE_SHOVEL, Element.CRYO),
            entry(PGCItems.ANOMALY_SHIVADA_JADE_PICKAXE, Element.CRYO));

    private ElementTools() {
    }

    @SubscribeEvent
    public static void onModifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        ELEMENTS.forEach((item, element) -> event.modify(item,
                builder -> builder.set(PGCDataComponents.ELEMENT_TYPE.get(), element.index())));
    }

    public static Map<DeferredItem<?>, Element> elements() {
        return ELEMENTS;
    }
}
