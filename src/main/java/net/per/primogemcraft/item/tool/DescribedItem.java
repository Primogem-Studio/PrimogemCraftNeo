package net.per.primogemcraft.item.tool;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DescribedItem extends Item {
    private static final String TOOLTIP_SUFFIX = ".tooltip.";

    public DescribedItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendDescription(stack, tooltip);
    }

    /**
     * Appends every {@code <description id>.tooltip.N} line, stopping at the first missing index.
     */
    public static void appendDescription(ItemStack stack, List<Component> tooltip) {
        var prefix = stack.getDescriptionId() + TOOLTIP_SUFFIX;
        for (var index = 0; Language.getInstance().has(prefix + index); index++) {
            tooltip.add(Component.translatable(prefix + index));
        }
    }

    protected static boolean described(ItemStack stack) {
        return Language.getInstance().has(stack.getDescriptionId() + TOOLTIP_SUFFIX + 0);
    }
}
