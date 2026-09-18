package net.per.primogemcraft.item.weapon;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.WeaponTier;

public class MoraPickaxeItem extends MoraToolItem {
    private static final Tier TIER = new WeaponTier(1024, 7.0F, 20, WeaponTier.IRON_INCORRECT, PGCItems.MORA);

    private static final float ATTACK_DAMAGE = 3.5F;
    private static final float ATTACK_SPEED = -2.8F;

    public MoraPickaxeItem(Properties properties) {
        super(TIER, properties, BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED, false);
    }
}
