package net.per.primogemcraft.item.weapon;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class MoraShovelItem extends MoraToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1024, 7.0F, 20, WeaponTier.IRON_INCORRECT, PGCItems.MORA);

    private static final float ATTACK_DAMAGE = 4.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public MoraShovelItem(Settings properties) {
        super(TIER, properties, BlockTags.SHOVEL_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED, false);
    }
}
