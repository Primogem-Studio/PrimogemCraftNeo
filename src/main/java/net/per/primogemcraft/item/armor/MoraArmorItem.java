package net.per.primogemcraft.item.armor;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;

public class MoraArmorItem extends ArmorItem {
    private static final int DURABILITY_MULTIPLIER = 64;

    public MoraArmorItem(ArmorItem.Type type, ArmorMaterial material, Settings properties) {
        super(material, type, properties.maxDamage(11 * DURABILITY_MULTIPLIER));
    }
}
