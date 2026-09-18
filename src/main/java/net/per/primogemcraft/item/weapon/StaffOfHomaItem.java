package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class StaffOfHomaItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(3000, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

    private static final double ATTACK_BASE = 0.12D;
    private static final double ATTACK_STEP = 0.03D;
    private static final double HEALTH_BASE = 0.2D;
    private static final double HEALTH_STEP = 0.05D;
    private static final double LOW_HEALTH_RATIO = 0.5D;
    private static final int LOW_HEALTH_MULTIPLIER = 2;
    private static final int ABILITY_COOLDOWN = 400;
    private static final int RESISTANCE_TICKS = 360;
    private static final int RESISTANCE_LEVEL = 1;
    private static final double HEALTH_COST = 0.3D;
    private static final int HEALTH_COST_PERCENT = 30;
    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String PASSIVE_ACTION = "passive";
    private static final String BASE_PASSIVE_ACTION = "base_passive";
    private static final String RESISTANCE_TEXT = "resistance";
    private static final String ATTACK_TEXT = "attack";
    private static final String HEALTH_TEXT = "health";

    public StaffOfHomaItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(),
                WeaponModifier.of(Attributes.MAX_HEALTH, HEALTH_BASE, HEALTH_STEP, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
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
                WeaponDescription.of(RIGHT_CLICK, RESISTANCE_TEXT,
                        WishReports.number(HEALTH_COST_PERCENT, ChatFormatting.AQUA),
                        WishReports.number(RESISTANCE_TICKS / 20, ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                        WishReports.percent(ATTACK_BASE + ATTACK_STEP * (refinement - 1), ChatFormatting.AQUA),
                        WishReports.percent((ATTACK_BASE + ATTACK_STEP * (refinement - 1)) * LOW_HEALTH_MULTIPLIER, ChatFormatting.AQUA)),
                WeaponDescription.of(BASE_PASSIVE_ACTION, HEALTH_TEXT,
                        WishReports.percent(HEALTH_BASE + HEALTH_STEP * (refinement - 1), ChatFormatting.AQUA)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        player.getCooldowns().addCooldown(stack.getItem(), ABILITY_COOLDOWN);
        player.setHealth((float) Math.max(1.0D, player.getHealth() - player.getHealth() * HEALTH_COST));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, RESISTANCE_TICKS, RESISTANCE_LEVEL, false, false));
        var low = player.getHealth() < player.getMaxHealth() * LOW_HEALTH_RATIO;
        level.playSound(null, player.blockPosition(), low ? SoundEvents.BLAZE_SHOOT : SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        return super.use(level, player, hand);
    }
}
