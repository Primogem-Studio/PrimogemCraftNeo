package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.util.PGCTimer;

import java.util.List;

public class SplendorOfTranquilWatersItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(3000, 7.0F, 15, WeaponTier.WOODEN_INCORRECT, PGCItems.JUSTICE_METAL);

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

    public SplendorOfTranquilWatersItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), WeaponModifier.of(EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_BASE, HEALTH_STEP, EntityAttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, REGEN_TEXT,
                        WishReports.number(regenLevel(refinement), Formatting.AQUA)),
                WeaponDescription.of(SNEAK_LEFT, TRADE_TEXT,
                        WishReports.percent(tradeRatio(refinement), Formatting.AQUA)),
                WeaponDescription.of(SNEAK_RIGHT, DAMAGE_TEXT,
                        WishReports.percent(tradeRatio(refinement), Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, HEALTH_TEXT,
                        WishReports.percent(HEALTH_BASE + HEALTH_STEP * (refinement - 1), Formatting.AQUA)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        var refinement = WeaponState.of(stack).refinements();
        if (player.isSneaking()) {
            level.playSound(null, player.getBlockPos(), PGCSounds.VARUNADA_LAZURITE_BUBBLE.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
            PGCTimer.clear(player, DAMAGE_MODE);
            PGCTimer.set(player, HEALTH_MODE, MODE_TICKS);
            player.getItemCooldownManager().set(stack.getItem(), MODE_COOLDOWN);
            return super.use(level, player, hand);
        }
        level.playSound(null, player.getBlockPos(), PGCSounds.VARUNADA_LAZURITE_BURST.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        var amplifier = regenLevel(refinement) - 1;
        if (player.getHealth() < player.getMaxHealth()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, EFFECT_TICKS, amplifier, false, true));
        } else if (level instanceof ServerWorld server) {
            for (var candidate : server.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(RADIUS), e -> true))
                if (candidate != player && candidate.isAlive()
                        && (candidate instanceof MobEntity mob && mob.getTarget() == player || candidate instanceof PlayerEntity))
                    candidate.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, EFFECT_TICKS, Math.min(WITHER_LEVEL_CAP, amplifier), false, true));
        }
        player.getItemCooldownManager().set(stack.getItem(), ABILITY_COOLDOWN);
        return super.use(level, player, hand);
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        var result = super.onEntitySwing(stack, entity);
        if (entity.getWorld().isClient() || !(entity instanceof PlayerEntity player)) return result;
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
        if (!player.isSneaking()) return result;
        player.getWorld().playSound(null, player.getBlockPos(), PGCSounds.VARUNADA_LAZURITE_BUBBLE.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.removeStatusEffect(StatusEffects.REGENERATION);
        PGCTimer.clear(player, HEALTH_MODE);
        PGCTimer.set(player, DAMAGE_MODE, MODE_TICKS);
        player.getItemCooldownManager().set(stack.getItem(), MODE_COOLDOWN);
        return result;
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        var ratio = tradeRatio(refinement);
        if (!PGCTimer.isDone(player, DAMAGE_MODE))
            return List.of(
                    WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, (owner, worn, value) -> ratio),
                    WeaponModifier.conditional(EntityAttributes.GENERIC_MAX_HEALTH, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, (owner, worn, value) -> -HEALTH_LOSS));
        if (!PGCTimer.isDone(player, HEALTH_MODE))
            return List.of(
                    WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, (owner, worn, value) -> -ATTACK_LOSS),
                    WeaponModifier.conditional(EntityAttributes.GENERIC_MAX_HEALTH, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, (owner, worn, value) -> ratio));
        return List.of();
    }

    private static int regenLevel(int refinement) {
        return (int) (REGEN_BASE + REGEN_STEP * (refinement - 1) * GenshinCraftIntegration.regenerationScale());
    }

    private static double tradeRatio(int refinement) {
        return TRADE_BASE + TRADE_STEP * (refinement - 1);
    }
}
