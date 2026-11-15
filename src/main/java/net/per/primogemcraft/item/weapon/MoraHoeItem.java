package net.per.primogemcraft.item.weapon;

import net.minecraft.registry.tag.BlockTags;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class MoraHoeItem extends MoraToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1024, 6.0F, 5, WeaponTier.IRON_INCORRECT, PGCItems.MORA);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public MoraHoeItem(Settings properties) {
        super(TIER, properties, BlockTags.HOE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED, true);
    }
}
