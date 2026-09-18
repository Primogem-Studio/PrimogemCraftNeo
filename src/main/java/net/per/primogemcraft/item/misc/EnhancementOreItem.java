package net.per.primogemcraft.item.misc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;

import java.util.List;

public class EnhancementOreItem extends DescribedItem {
    private static final String SINGLE_SUFFIX = ".xp";
    private static final String STACK_SUFFIX = ".xp.stack";

    public EnhancementOreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var xp = WeaponEnhancement.xpPerOre(stack.getItem());
        if (xp <= 0) return;
        var prefix = stack.getDescriptionId();
        tooltip.add(Component.translatable(prefix + SINGLE_SUFFIX, xp));
        tooltip.add(Component.translatable(prefix + STACK_SUFFIX, xp * stack.getCount()));
    }
}
