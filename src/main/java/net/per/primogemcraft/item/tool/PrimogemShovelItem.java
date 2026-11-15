package net.per.primogemcraft.item.tool;

import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class PrimogemShovelItem extends ShovelItem {
    private static final ToolMaterial TIER = new WeaponTier(320, 4.0F, 15, WeaponTier.DIAMOND_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PrimogemShovelItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }
}
