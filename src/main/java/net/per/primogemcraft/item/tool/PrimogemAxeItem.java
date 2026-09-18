package net.per.primogemcraft.item.tool;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class PrimogemAxeItem extends AxeItem {
    private static final Tier TIER = new WeaponTier(320, 8.5F, 15, WeaponTier.STONE_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PrimogemAxeItem(Properties properties) {
        super(TIER, properties.attributes(DiggerItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }
}
