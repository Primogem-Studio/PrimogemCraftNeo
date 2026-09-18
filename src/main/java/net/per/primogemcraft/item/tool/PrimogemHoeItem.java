package net.per.primogemcraft.item.tool;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class PrimogemHoeItem extends HoeItem {
    private static final Tier TIER = new WeaponTier(320, 8.5F, 10, WeaponTier.DIAMOND_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PrimogemHoeItem(Properties properties) {
        super(TIER, properties.attributes(DiggerItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }
}
