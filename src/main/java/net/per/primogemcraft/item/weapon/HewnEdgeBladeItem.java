package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class HewnEdgeBladeItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(3600, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.STURDY_METAL);

    private static final int MAX_STACKS = 8;
    private static final int ABSORPTION_TICKS = 600;
    private static final int USE_COOLDOWN = 160;
    private static final double STACK_CHANCE_BASE = 0.2D;
    private static final double STACK_CHANCE_STEP = 0.05D;
    private static final double HEALTH_COST = 0.2D;
    private static final double HEAL_BASE = 0.4D;
    private static final double HEAL_STEP = 0.1D;
    private static final int HEAL_COOLDOWN_BASE = 2400;
    private static final int HEAL_COOLDOWN_STEP = 300;
    private static final int ABSORPTION_MINUTES_BASE = 1200;
    private static final int ABSORPTION_MINUTES_STEP = 300;
    private static final int TICKS_PER_MINUTE = 1200;
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -2.4F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String RIGHT_CLICK = "right_click";
    private static final String SNEAK_USE = "sneak_use";
    private static final String ABSORPTION_TEXT = "absorption";
    private static final String USE_TEXT = "use";
    private static final String CONSUME_TEXT = "consume";

    public HewnEdgeBladeItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, ABSORPTION_TEXT,
                        WishReports.percent(stackChance(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(RIGHT_CLICK, USE_TEXT,
                        WishReports.number(refinement, ChatFormatting.AQUA),
                        WishReports.number((double) absorptionTicks(refinement) / TICKS_PER_MINUTE, ChatFormatting.AQUA)),
                WeaponDescription.of(SNEAK_USE, CONSUME_TEXT,
                        WishReports.percent(healRatio(refinement), ChatFormatting.AQUA),
                        WishReports.number(healCooldown(refinement) / 20, ChatFormatting.AQUA)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.temporary(WeaponCharge.of(stack), MAX_STACKS);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return result;
        var refinement = WeaponState.of(stack).refinements();
        var stacks = WeaponCharge.of(stack);
        if (stacks >= MAX_STACKS) {
            WeaponCharge.set(stack, 0);
            refreshAbsorption(attacker);
            level.playSound(null, attacker.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.NEUTRAL, 0.5F, 0.5F);
            return result;
        }
        if (level.getRandom().nextDouble() >= stackChance(refinement)) return result;
        WeaponCharge.set(stack, stacks + 1);
        level.playSound(null, attacker.blockPosition(), PGCSounds.WEAPON_CHARGE.get(), SoundSource.PLAYERS, 2.0F, 5.0F);
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isShiftKeyDown()) {
            if (player.getEffect(MobEffects.ABSORPTION) == null) return super.use(level, player, hand);
            player.removeEffect(MobEffects.ABSORPTION);
            player.heal((float) (player.getMaxHealth() * healRatio(refinement)));
            player.getCooldowns().addCooldown(stack.getItem(), healCooldown(refinement));
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.5F, 3.0F);
            return super.use(level, player, hand);
        }
        player.removeEffect(MobEffects.ABSORPTION);
        player.hurt(level.damageSources().genericKill(), (float) (player.getHealth() * HEALTH_COST));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, absorptionTicks(refinement), refinement - 1, false, true));
        player.getCooldowns().addCooldown(stack.getItem(), USE_COOLDOWN);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5F, 0.6F);
        return super.use(level, player, hand);
    }

    private static void refreshAbsorption(LivingEntity entity) {
        var absorption = entity.getEffect(MobEffects.ABSORPTION);
        var amplifier = absorption == null || absorption.getAmplifier() <= 1 ? 0 : absorption.getAmplifier();
        var duration = absorption == null || absorption.getDuration() <= 0 ? ABSORPTION_TICKS : absorption.getDuration();
        entity.removeEffect(MobEffects.ABSORPTION);
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, amplifier));
    }

    private static double stackChance(int refinement) {
        return STACK_CHANCE_BASE + STACK_CHANCE_STEP * (refinement - 1);
    }

    private static double healRatio(int refinement) {
        return HEAL_BASE + HEAL_STEP * (refinement - 1);
    }

    private static int absorptionTicks(int refinement) {
        return ABSORPTION_MINUTES_BASE + ABSORPTION_MINUTES_STEP * (refinement - 1);
    }

    private static int healCooldown(int refinement) {
        return Math.max(20, HEAL_COOLDOWN_BASE - HEAL_COOLDOWN_STEP * (refinement - 1));
    }
}
