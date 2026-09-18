package net.per.primogemcraft.item.misc;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.element.Element;

import java.util.List;

public class ElementalMoltenBeadItem extends Item {
    private static final String TOOLTIP_SUFFIX = ".tooltip.";

    public ElementalMoltenBeadItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        var element = Element.of(stack);
        return element == null ? super.getName(stack) : Component.translatable(stack.getDescriptionId() + "." + element.id());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!level.isClientSide) activate(stack, player.getRandom());
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var element = Element.of(stack);
        var prefix = stack.getDescriptionId() + (element == null ? "" : "." + element.id()) + TOOLTIP_SUFFIX;
        for (var index = 0; Language.getInstance().has(prefix + index); index++)
            tooltip.add(Component.translatable(prefix + index));
    }

    private static void activate(ItemStack stack, RandomSource random) {
        if (Element.of(stack) != null) return;
        stack.set(PGCDataComponents.ELEMENT_TYPE.get(), random.nextInt(Element.values().length) + 1);
    }
}
