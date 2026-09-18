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
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class PrimordialJadeCutterItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(2000, 8.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.PRITHIVA_TOPAZ_FRAGMENT);

    private static final double HEAL_RATIO = 0.01D;
    private static final int COOLDOWN_BASE = 160;
    private static final int COOLDOWN_STEP = 20;
    private static final int MIN_COOLDOWN = 20;
    private static final double ATTACK_BASE = 0.12D;
    private static final double ATTACK_STEP = 0.03D;
    private static final double BASE_BASE = 0.1D;
    private static final double BASE_STEP = 0.025D;
    private static final double LOW_HEALTH_RATIO = 0.5D;
    private static final int LOW_HEALTH_MULTIPLIER = 2;
    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String BASE_PASSIVE_ACTION = "base_passive";
    private static final String DRAIN_TEXT = "drain";
    private static final String HEALTH_TEXT = "health";
    private static final String BASE_HEALTH_TEXT = "base_health";
    private static final String BASE_ATTACK_TEXT = "base_attack";

    public PrimordialJadeCutterItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(),
                WeaponModifier.of(Attributes.MAX_HEALTH, BASE_BASE, BASE_STEP, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                WeaponModifier.of(Attributes.ATTACK_DAMAGE, BASE_BASE, BASE_STEP, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        return List.of(WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_VALUE,
                (owner, worn, value) -> {
                    var scale = owner.getHealth() < owner.getMaxHealth() * LOW_HEALTH_RATIO ? LOW_HEALTH_MULTIPLIER : 1;
                    var ratio = (ATTACK_BASE + ATTACK_STEP * (value - 1)) * GenshinCraftIntegration.healthConversionScale();
                    return owner.getMaxHealth() * ratio * scale;
                }));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, DRAIN_TEXT,
                        WishReports.percent(HEAL_RATIO, ChatFormatting.AQUA),
                        WishReports.number(cooldownSeconds(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, HEALTH_TEXT,
                        WishReports.percent(ATTACK_BASE + ATTACK_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.of(BASE_PASSIVE_ACTION, BASE_HEALTH_TEXT,
                        WishReports.percent(BASE_BASE + BASE_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.note(BASE_ATTACK_TEXT,
                        WishReports.percent(BASE_BASE + BASE_STEP * (refinement - 1), ChatFormatting.AQUA)));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel)) return result;
        if (attacker instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem())) return result;
        attacker.heal(Math.max(1.0F, (float) (target.getHealth() * HEAL_RATIO)));
        if (attacker instanceof Player player)
            player.getCooldowns().addCooldown(stack.getItem(), cooldownTicks(WeaponState.of(stack).refinements()));
        return result;
    }

    private static int cooldownTicks(int refinement) {
        return Math.max(MIN_COOLDOWN, COOLDOWN_BASE - COOLDOWN_STEP * (refinement - 1));
    }

    private static int cooldownSeconds(int refinement) {
        return cooldownTicks(refinement) / 20;
    }
}
