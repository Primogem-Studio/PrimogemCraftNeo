package net.per.primogemcraft.item.tool;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class PrimogemShovelItem extends ShovelItem {
    private static final Tier TIER = new WeaponTier(320, 4.0F, 15, WeaponTier.DIAMOND_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PrimogemShovelItem(Properties properties) {
        super(TIER, properties.attributes(DiggerItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }
}
