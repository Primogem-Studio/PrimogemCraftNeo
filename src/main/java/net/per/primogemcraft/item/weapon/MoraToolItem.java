package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponModifier;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public abstract class MoraToolItem extends WishWeaponToolItem {
    private static final double ATTACK_BASE = 2.0D;
    private static final double ATTACK_STEP = 0.5D;
    private static final double MINING_BASE = 1.0D;
    private static final double MINING_STEP = 0.25D;
    private static final String PASSIVE_ACTION = "passive";
    private static final String ATTACK_TEXT = "attack";
    private static final String MINING_TEXT = "mining";

    private final boolean attacks;

    protected MoraToolItem(Tier tier, Properties properties, TagKey<Block> mineable, float attackDamage, float attackSpeed, boolean attacks) {
        super(tier, properties.fireResistant(), mineable, attackDamage, attackSpeed, passives(attacks));
        this.attacks = attacks;
    }

    private static WeaponModifier[] passives(boolean attacks) {
        var mining = WeaponModifier.of(Attributes.MINING_EFFICIENCY, MINING_BASE, MINING_STEP, AttributeModifier.Operation.ADD_VALUE);
        if (!attacks) return new WeaponModifier[]{mining};
        return new WeaponModifier[]{
                WeaponModifier.of(Attributes.ATTACK_DAMAGE, ATTACK_BASE, ATTACK_STEP, AttributeModifier.Operation.ADD_VALUE),
                mining};
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        var mining = WeaponDescription.of(PASSIVE_ACTION, MINING_TEXT,
                WishReports.number(MINING_BASE + MINING_STEP * (refinement - 1), ChatFormatting.AQUA));
        if (!attacks) return List.of(mining);
        return List.of(
                WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                        WishReports.number(ATTACK_BASE + ATTACK_STEP * (refinement - 1), ChatFormatting.AQUA)),
                mining);
    }
}
