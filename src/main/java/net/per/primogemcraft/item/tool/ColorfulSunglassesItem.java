package net.per.primogemcraft.item.tool;

import net.minecraft.world.World;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;

import java.util.List;

public class ColorfulSunglassesItem extends ArmorItem {
    public ColorfulSunglassesItem(ArmorMaterial material, Settings properties) {
        super(material, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public void appendTooltip(ItemStack stack, @org.jetbrains.annotations.Nullable World level, List<Text> tooltip, TooltipContext flag) {
        tooltip.add(Text.translatable("item.primogemcraft.colorful_sunglasses.tooltip.0"));
        tooltip.add(Text.translatable("item.primogemcraft.colorful_sunglasses.tooltip.1"));
        tooltip.add(Text.translatable("item.primogemcraft.colorful_sunglasses.tooltip.2"));
    }
}
