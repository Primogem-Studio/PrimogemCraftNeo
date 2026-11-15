package net.per.primogemcraft.item.tool;

import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.block.Block;

import java.util.List;

public class DescribedBlockItem extends BlockItem {
    public DescribedBlockItem(Block block, Settings properties) {
        super(block, properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        DescribedItem.appendDescription(stack, tooltip);
    }
}
