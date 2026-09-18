package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

public class SplendorOfTranquilWatersItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(3000, 7.0F, 15, WeaponTier.WOODEN_INCORRECT, PGCItems.JUSTICE_METAL);

    private static final String DAMAGE_MODE = "tranquil_damage";
    private static final String HEALTH_MODE = "tranquil_health";
    private static final int ABILITY_COOLDOWN = 400;
    private static final int MODE_COOLDOWN = 10;
    private static final int MODE_TICKS = 400;
    private static final int EFFECT_TICKS = 200;
    private static final double RADIUS = 8.0D;
    private static final double HEALTH_BASE = 0.24D;
    private static final double HEALTH_STEP = 0.06D;
    private static final double REGEN_BASE = 1.0D;
    private static final double REGEN_STEP = 1.0D;
    private static final double TRADE_BASE = 0.22D;
    private static final double TRADE_STEP = 0.055D;
    private static final double HEALTH_LOSS = 0.2D;
    private static final double ATTACK_LOSS = 0.2D;
    private static final int WITHER_LEVEL_CAP = 32;
    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String SNEAK_LEFT = "sneak_swing";
    private static final String SNEAK_RIGHT = "sneak_use";
    private static final String PASSIVE_ACTION = "passive";
    private static final String REGEN_TEXT = "regen";
    private static final String TRADE_TEXT = "trade";
    private static final String DAMAGE_TEXT = "damage";
    private static final String HEALTH_TEXT = "health";

    public SplendorOfTranquilWatersItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant(),
                WeaponModifier.of(Attributes.MAX_HEALTH, HEALTH_BASE, HEALTH_STEP, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, REGEN_TEXT,
                        WishReports.number(regenLevel(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(SNEAK_LEFT, TRADE_TEXT,
                        WishReports.percent(tradeRatio(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(SNEAK_RIGHT, DAMAGE_TEXT,
                        WishReports.percent(tradeRatio(refinement), ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, HEALTH_TEXT,
                        WishReports.percent(HEALTH_BASE + HEALTH_STEP * (refinement - 1), ChatFormatting.AQUA)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (level.isClientSide() || player.getCooldowns().isOnCooldown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isShiftKeyDown()) {
            level.playSound(null, player.blockPosition(), PGCSounds.VARUNADA_LAZURITE_BUBBLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            PGCTimer.clear(player, DAMAGE_MODE);
            PGCTimer.set(player, HEALTH_MODE, MODE_TICKS);
            player.getCooldowns().addCooldown(stack.getItem(), MODE_COOLDOWN);
            return super.use(level, player, hand);
        }
        level.playSound(null, player.blockPosition(), PGCSounds.VARUNADA_LAZURITE_BURST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        var amplifier = regenLevel(refinement) - 1;
        if (player.getHealth() < player.getMaxHealth()) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_TICKS, amplifier, false, true));
        } else if (level instanceof ServerLevel server) {
            for (var candidate : server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS)))
                if (candidate != player && candidate.isAlive()
                        && (candidate instanceof Mob mob && mob.getTarget() == player || candidate instanceof Player))
                    candidate.addEffect(new MobEffectInstance(MobEffects.WITHER, EFFECT_TICKS, Math.min(WITHER_LEVEL_CAP, amplifier), false, true));
        }
        player.getCooldowns().addCooldown(stack.getItem(), ABILITY_COOLDOWN);
        return super.use(level, player, hand);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        var result = super.onEntitySwing(stack, entity, hand);
        if (entity.level().isClientSide() || !(entity instanceof Player player)) return result;
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return result;
        if (!player.isShiftKeyDown()) return result;
        player.level().playSound(null, player.blockPosition(), PGCSounds.VARUNADA_LAZURITE_BUBBLE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.removeEffect(MobEffects.REGENERATION);
        PGCTimer.clear(player, HEALTH_MODE);
        PGCTimer.set(player, DAMAGE_MODE, MODE_TICKS);
        player.getCooldowns().addCooldown(stack.getItem(), MODE_COOLDOWN);
        return result;
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var ratio = tradeRatio(refinement);
        if (!PGCTimer.isDone(player, DAMAGE_MODE))
            return List.of(
                    WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, (owner, worn, value) -> ratio),
                    WeaponModifier.conditional(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, (owner, worn, value) -> -HEALTH_LOSS));
        if (!PGCTimer.isDone(player, HEALTH_MODE))
            return List.of(
                    WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, (owner, worn, value) -> -ATTACK_LOSS),
                    WeaponModifier.conditional(Attributes.MAX_HEALTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, (owner, worn, value) -> ratio));
        return List.of();
    }

    private static int regenLevel(int refinement) {
        return (int) (REGEN_BASE + REGEN_STEP * (refinement - 1) * GenshinCraftIntegration.regenerationScale());
    }

    private static double tradeRatio(int refinement) {
        return TRADE_BASE + TRADE_STEP * (refinement - 1);
    }
}
