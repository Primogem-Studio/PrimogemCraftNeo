package net.per.primogemcraft.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.per.primogemcraft.system.element.Element;

import java.util.ArrayList;
import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class ElementTrimTooltips {
    private static final String TRIM_PREFIX = "trim_material." + MOD_ID + ".";
    private static final String TOOLTIP_SUFFIX = ".tooltip.";

    private ElementTrimTooltips() {
    }

    @SubscribeEvent
    public static void addLines(ItemTooltipEvent event) {
        var trim = event.getItemStack().get(DataComponents.TRIM);
        if (trim == null) return;
        var element = Element.ofTrimMaterial(trim.material());
        if (element == null) return;
        var tooltip = event.getToolTip();
        var material = trim.material().value();
        var materialLine = CommonComponents.space().append(material.description());
        for (var index = 0; index < tooltip.size(); index++) {
            if (!materialLine.equals(tooltip.get(index))) continue;
            var lines = lines(element, material.description().getStyle().getColor());
            for (var offset = 0; offset < lines.size(); offset++) tooltip.add(index + 1 + offset, lines.get(offset));
            return;
        }
    }

    private static List<Component> lines(Element element, TextColor color) {
        var prefix = TRIM_PREFIX + element.id() + TOOLTIP_SUFFIX;
        var tooltip = new ArrayList<Component>();
        var index = 0;
        tooltip.add(line(prefix + index++, color));
        tooltip.add(line(prefix + index++, color));
        if (!Screen.hasShiftDown()) return tooltip;
        for (var line = 0; line < element.trimTooltipShiftLines(); line++)
            tooltip.add(line(prefix + index++, color));
        if (element.trimTooltipControlLines() <= 0) return tooltip;
        tooltip.add(line(prefix + index++, color));
        if (!Screen.hasControlDown()) return tooltip;
        for (var line = 0; line < element.trimTooltipControlLines(); line++)
            tooltip.add(line(prefix + index++, color));
        return tooltip;
    }

    private static Component line(String key, TextColor color) {
        var line = Component.translatable(key);
        return color == null ? line : line.withStyle(style -> style.withColor(color));
    }
}
