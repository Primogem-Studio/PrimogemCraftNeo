package net.per.primogemcraft.client;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.per.primogemcraft.item.misc.UpgradeSmithingTemplateItem;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class SmithingTemplateTooltips {
    private static final String SHARED_PREFIX = "smithing_template." + MOD_ID + ".";
    private static final String TOOLTIP_SUFFIX = ".tooltip.";

    private SmithingTemplateTooltips() {
    }

    @SubscribeEvent
    public static void gatherComponents(RenderTooltipEvent.GatherComponents event) {
        var stack = event.getItemStack();
        if (!(stack.getItem() instanceof UpgradeSmithingTemplateItem)) return;
        var elements = event.getTooltipElements();
        var prefix = stack.getDescriptionId() + TOOLTIP_SUFFIX;
        elements.add(Either.left(Component.translatable(prefix + 0)));
        elements.add(Either.left(Component.translatable(SHARED_PREFIX + "blank")));
        elements.add(Either.left(Component.translatable(SHARED_PREFIX + "applies")));
        elements.add(Either.left(Component.translatable(prefix + 1)));
        elements.add(Either.left(Component.translatable(SHARED_PREFIX + "materials")));
        elements.add(Either.left(Component.translatable(prefix + 2)));
        elements.add(Either.left(Component.translatable(SHARED_PREFIX + "set_value")));
        elements.add(Either.left(Component.translatable(prefix + 3)));
    }
}
