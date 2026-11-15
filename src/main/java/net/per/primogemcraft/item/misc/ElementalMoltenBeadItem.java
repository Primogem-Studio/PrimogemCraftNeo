package net.per.primogemcraft.item.misc;

import net.per.primogemcraft.compat.component.StackComponents;
import net.minecraft.util.Language;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.element.Element;

import java.util.List;

public class ElementalMoltenBeadItem extends Item {
    private static final String TOOLTIP_SUFFIX = ".tooltip.";

    public ElementalMoltenBeadItem(Settings properties) {
        super(properties);
    }

    @Override
    public Text getName(ItemStack stack) {
        var element = Element.of(stack);
        return element == null ? super.getName(stack) : Text.translatable(stack.getTranslationKey() + "." + element.id());
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!level.isClient) activate(stack, player.getRandom());
        return TypedActionResult.success(stack, level.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        var element = Element.of(stack);
        var prefix = stack.getTranslationKey() + (element == null ? "" : "." + element.id()) + TOOLTIP_SUFFIX;
        for (var index = 0; Language.getInstance().hasTranslation(prefix + index); index++)
            tooltip.add(Text.translatable(prefix + index));
    }

    private static void activate(ItemStack stack, Random random) {
        if (Element.of(stack) != null) return;
        StackComponents.set(stack, PGCDataComponents.ELEMENT_TYPE, random.nextInt(Element.values().length) + 1);
    }
}
