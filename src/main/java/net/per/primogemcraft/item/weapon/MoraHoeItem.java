package net.per.primogemcraft.item.weapon;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class MoraHoeItem extends MoraToolItem {
    private static final Tier TIER = new WeaponTier(1024, 6.0F, 5, WeaponTier.IRON_INCORRECT, PGCItems.MORA);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public MoraHoeItem(Properties properties) {
        super(TIER, properties, BlockTags.MINEABLE_WITH_HOE, ATTACK_DAMAGE, ATTACK_SPEED, true);
    }
}
