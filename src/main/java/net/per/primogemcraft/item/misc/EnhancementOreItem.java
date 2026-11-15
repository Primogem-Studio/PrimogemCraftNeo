package net.per.primogemcraft.item.misc;

import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;

import java.util.List;

public class EnhancementOreItem extends DescribedItem {
    private static final String SINGLE_SUFFIX = ".xp";
    private static final String STACK_SUFFIX = ".xp.stack";

    public EnhancementOreItem(Settings properties) {
        super(properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        super.appendTooltip(stack, level, tooltip, flag);
        var xp = WeaponEnhancement.xpPerOre(stack.getItem());
        if (xp <= 0) return;
        var prefix = stack.getTranslationKey();
        tooltip.add(Text.translatable(prefix + SINGLE_SUFFIX, xp));
        tooltip.add(Text.translatable(prefix + STACK_SUFFIX, xp * stack.getCount()));
    }
}
