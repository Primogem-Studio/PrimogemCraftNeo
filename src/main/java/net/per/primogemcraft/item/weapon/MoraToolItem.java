package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.block.Block;
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

    protected MoraToolItem(ToolMaterial tier, Settings properties, TagKey<Block> mineable, float attackDamage, float attackSpeed, boolean attacks) {
        super(tier, properties.fireproof(), mineable, attackDamage, attackSpeed, passives(attacks));
        this.attacks = attacks;
    }

    private static WeaponModifier[] passives(boolean attacks) {
        // 1.20.1 没有 MINING_EFFICIENCY 属性（挖掘速度由 Tier 决定），
        // 这里改用 ATTACK_DAMAGE 占位以保留被动结构；实际挖掘速度由材质等级提供。
        var mining = WeaponModifier.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, MINING_BASE, MINING_STEP, EntityAttributeModifier.Operation.ADDITION);
        if (!attacks) return new WeaponModifier[]{mining};
        return new WeaponModifier[]{
                WeaponModifier.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, ATTACK_BASE, ATTACK_STEP, EntityAttributeModifier.Operation.ADDITION),
                mining};
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        var mining = WeaponDescription.of(PASSIVE_ACTION, MINING_TEXT,
                WishReports.number(MINING_BASE + MINING_STEP * (refinement - 1), Formatting.AQUA));
        if (!attacks) return List.of(mining);
        return List.of(
                WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                        WishReports.number(ATTACK_BASE + ATTACK_STEP * (refinement - 1), Formatting.AQUA)),
                mining);
    }
}
