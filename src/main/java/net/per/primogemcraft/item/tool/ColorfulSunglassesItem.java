package net.per.primogemcraft.item.tool;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ColorfulSunglassesItem extends ArmorItem {
    public ColorfulSunglassesItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.primogemcraft.colorful_sunglasses.tooltip.0"));
        tooltip.add(Component.translatable("item.primogemcraft.colorful_sunglasses.tooltip.1"));
        tooltip.add(Component.translatable("item.primogemcraft.colorful_sunglasses.tooltip.2"));
    }
}
