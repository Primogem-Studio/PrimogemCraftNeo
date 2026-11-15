package net.per.primogemcraft.item.tool;

import net.minecraft.item.AxeItem;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class PrimogemAxeItem extends AxeItem {
    private static final ToolMaterial TIER = new WeaponTier(320, 8.5F, 15, WeaponTier.STONE_INCORRECT, PGCItems.PRIMOGEM);
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public PrimogemAxeItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }
}
