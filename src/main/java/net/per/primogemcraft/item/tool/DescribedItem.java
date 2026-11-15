package net.per.primogemcraft.item.tool;

import net.minecraft.world.World;
import net.minecraft.util.Language;
import net.minecraft.text.Text;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;

import java.util.List;

public class DescribedItem extends Item {
    private static final String TOOLTIP_SUFFIX = ".tooltip.";

    public DescribedItem(Settings properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        appendDescription(stack, tooltip);
    }

    /**
     * Appends every {@code <description id>.tooltip.N} line, stopping at the first missing index.
     */
    public static void appendDescription(ItemStack stack, List<Text> tooltip) {
        var prefix = stack.getTranslationKey() + TOOLTIP_SUFFIX;
        for (var index = 0; Language.getInstance().hasTranslation(prefix + index); index++) {
            tooltip.add(Text.translatable(prefix + index));
        }
    }

    protected static boolean described(ItemStack stack) {
        return Language.getInstance().hasTranslation(stack.getTranslationKey() + TOOLTIP_SUFFIX + 0);
    }
}
