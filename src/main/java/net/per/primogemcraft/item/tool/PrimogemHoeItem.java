package net.per.primogemcraft.item.tool;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.per.primogemcraft.compat.item.WeaponAttributes120;

public class PrimogemHoeItem extends HoeItem {
    private static final Tier TIER = new WeaponTier(320, 8.5F, 10, WeaponTier.DIAMOND_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PrimogemHoeItem(Properties properties) {
        super(TIER, 0, ATTACK_SPEED, properties.fireResistant());
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        var mutable = com.google.common.collect.ArrayListMultimap.<net.minecraft.world.entity.ai.attributes.Attribute, AttributeModifier>create();
        mutable.putAll(super.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND)
            WeaponAttributes120.putWeaponModifiers(mutable, ATTACK_DAMAGE, ATTACK_SPEED);
        return com.google.common.collect.ImmutableMultimap.copyOf(mutable);
    }
}
