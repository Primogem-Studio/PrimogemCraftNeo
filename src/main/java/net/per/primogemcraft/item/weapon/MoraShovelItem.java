package net.per.primogemcraft.item.weapon;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class MoraShovelItem extends MoraToolItem {
    private static final Tier TIER = new WeaponTier(1024, 7.0F, 20, WeaponTier.IRON_INCORRECT, PGCItems.MORA);

    private static final float ATTACK_DAMAGE = 4.0F;
    private static final float ATTACK_SPEED = -3.0F;

    public MoraShovelItem(Properties properties) {
        super(TIER, properties, BlockTags.MINEABLE_WITH_SHOVEL, ATTACK_DAMAGE, ATTACK_SPEED, false);
    }
}
