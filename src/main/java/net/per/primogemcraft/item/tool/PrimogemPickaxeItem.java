package net.per.primogemcraft.item.tool;

import net.minecraft.item.MiningToolItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public class PrimogemPickaxeItem extends PickaxeItem {
    private static final ToolMaterial TIER = new WeaponTier(320, 8.5F, 15, WeaponTier.STONE_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 4.5F;
    private static final float ATTACK_SPEED = -2.8F;

    public PrimogemPickaxeItem(Settings properties) {
        super(TIER, 0, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var mutable = com.google.common.collect.ArrayListMultimap.<net.minecraft.entity.attribute.EntityAttribute, EntityAttributeModifier>create();
        mutable.putAll(super.getAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND)
            WeaponAttributes120.putWeaponModifiers(mutable, ATTACK_DAMAGE, ATTACK_SPEED);
        return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
    }
}
