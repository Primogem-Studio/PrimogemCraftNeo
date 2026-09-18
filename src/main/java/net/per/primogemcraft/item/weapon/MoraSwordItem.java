package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class MoraSwordItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(1024, 7.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.MORA);

    private static final double ATTACK_BASE = 2.0D;
    private static final double ATTACK_STEP = 0.5D;
    private static final float ATTACK_DAMAGE = 3.5F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final String PASSIVE_ACTION = "passive";
    private static final String ATTACK_TEXT = "attack";

    public MoraSwordItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(),
                WeaponModifier.of(Attributes.ATTACK_DAMAGE, ATTACK_BASE, ATTACK_STEP, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        return List.of(WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                WishReports.number(ATTACK_BASE + ATTACK_STEP * (state.refinements() - 1), ChatFormatting.AQUA)));
    }
}
