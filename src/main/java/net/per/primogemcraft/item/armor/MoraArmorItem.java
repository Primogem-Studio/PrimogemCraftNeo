package net.per.primogemcraft.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class MoraArmorItem extends ArmorItem {
    private static final int DURABILITY_MULTIPLIER = 64;

    public MoraArmorItem(ArmorItem.Type type, Holder<ArmorMaterial> material, Properties properties) {
        super(material, type, properties.durability(type.getDurability(DURABILITY_MULTIPLIER)));
    }
}
