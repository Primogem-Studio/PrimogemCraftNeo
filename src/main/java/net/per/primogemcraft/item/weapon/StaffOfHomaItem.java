package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class StaffOfHomaItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(3000, 4.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

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

    public StaffOfHomaItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), WeaponModifier.of(EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_BASE, HEALTH_STEP, EntityAttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack)) return List.of();
        return List.of(WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.ADDITION,
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
                        WishReports.number(HEALTH_COST_PERCENT, Formatting.AQUA),
                        WishReports.number(RESISTANCE_TICKS / 20, Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                        WishReports.percent(ATTACK_BASE + ATTACK_STEP * (refinement - 1), Formatting.AQUA),
                        WishReports.percent((ATTACK_BASE + ATTACK_STEP * (refinement - 1)) * LOW_HEALTH_MULTIPLIER, Formatting.AQUA)),
                WeaponDescription.of(BASE_PASSIVE_ACTION, HEALTH_TEXT,
                        WishReports.percent(HEALTH_BASE + HEALTH_STEP * (refinement - 1), Formatting.AQUA)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        player.getItemCooldownManager().set(stack.getItem(), ABILITY_COOLDOWN);
        player.setHealth((float) Math.max(1.0D, player.getHealth() - player.getHealth() * HEALTH_COST));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, RESISTANCE_TICKS, RESISTANCE_LEVEL, false, false));
        var low = player.getHealth() < player.getMaxHealth() * LOW_HEALTH_RATIO;
        level.playSound(null, player.getBlockPos(), low ? SoundEvents.ENTITY_BLAZE_SHOOT : SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0F, 1.0F);
        return super.use(level, player, hand);
    }
}
