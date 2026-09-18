package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class TheBlackSwordItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(1500, 5.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.PRITHIVA_TOPAZ_FRAGMENT);

    private static final WeaponModifier PASSIVE = WeaponModifier.of(Attributes.ATTACK_DAMAGE, 0.2D, 0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final double HEAL_BASE = 0.3D;
    private static final double HEAL_STEP = 0.075D;
    private static final double HEAL_CHANCE = 0.5D;
    private static final int COOLDOWN_TICKS = 400;
    private static final int COOLDOWN_SECONDS = COOLDOWN_TICKS / 20;
    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String HEAL_TEXT = "heal";
    private static final String ATTACK_TEXT = "attack";

    public TheBlackSwordItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(), PASSIVE);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel level
                && !(attacker instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem()))
                && level.getRandom().nextDouble() >= HEAL_CHANCE) {
            attacker.heal((float) (attacker.getMaxHealth() * healRatio(WeaponState.of(stack).refinements())));
            if (attacker instanceof Player player) player.getCooldowns().addCooldown(stack.getItem(), COOLDOWN_TICKS);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, HEAL_TEXT,
                        WishReports.percent(healRatio(refinement), ChatFormatting.AQUA),
                        WishReports.number(COOLDOWN_SECONDS, ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                        WishReports.percent(PASSIVE.amount(refinement), ChatFormatting.AQUA)));
    }

    private static double healRatio(int refinement) {
        return HEAL_BASE + HEAL_STEP * (refinement - 1);
    }
}
