package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class SkywardBladeItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(1000, 5.0F, 20, WeaponTier.WOODEN_INCORRECT, Ingredient.EMPTY);

    private static final double SPEED_BASE = 1.0D;
    private static final double SPEED_STEP = 0.5D;
    private static final double MOVE_BASE = 0.01D;
    private static final double MOVE_STEP = 0.01D;
    private static final double MOVE_DISPLAY = 10.0D;
    private static final double STRIKE_BASE = 0.14D;
    private static final double STRIKE_STEP = 0.035D;
    private static final double POWER_BASE = 0.08D;
    private static final double POWER_STEP = 0.02D;
    private static final int ACTIVE_HITS = 12;
    private static final int ABILITY_COOLDOWN = 300;
    private static final int REFRESH_THRESHOLD = 3;
    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String RESONANCE_TEXT = "resonance";
    private static final String STRIKE_TEXT = "strike";
    private static final String POWER_TEXT = "power";

    public SkywardBladeItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), WeaponModifier.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, POWER_BASE, POWER_STEP, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        if (isHeld(player, slot, stack) || WeaponCharge.of(stack) <= 0) return List.of();
        return List.of(
                WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_SPEED, EntityAttributeModifier.Operation.ADDITION,
                        (owner, worn, value) -> SPEED_BASE + SPEED_STEP * (value - 1)),
                WeaponModifier.conditional(EntityAttributes.GENERIC_MOVEMENT_SPEED, EntityAttributeModifier.Operation.ADDITION,
                        (owner, worn, value) -> MOVE_BASE + MOVE_STEP * (value - 1)));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(RIGHT_CLICK, RESONANCE_TEXT,
                        WishReports.number(speedBonus(refinement), Formatting.AQUA),
                        WishReports.percent(moveBonus(refinement) * MOVE_DISPLAY, Formatting.AQUA)),
                WeaponDescription.of(NORMAL_ATTACK, STRIKE_TEXT,
                        WishReports.percent(strikeRatio(refinement), Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, POWER_TEXT,
                        WishReports.percent(powerBonus(refinement), Formatting.AQUA)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (level.isClient() || player.getItemCooldownManager().isCoolingDown(stack.getItem())) return super.use(level, player, hand);
        if (WeaponCharge.of(stack) >= REFRESH_THRESHOLD) return super.use(level, player, hand);
        WeaponCharge.set(stack, ACTIVE_HITS);
        player.getItemCooldownManager().set(stack.getItem(), ABILITY_COOLDOWN);
        level.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS, 1.5F, 3.0F);
        return super.use(level, player, hand);
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.temporary(WeaponCharge.of(stack), ACTIVE_HITS);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld level)) return result;
        var remaining = WeaponCharge.of(stack);
        if (remaining <= 0) return result;
        var damage = (float) (attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * strikeRatio(WeaponState.of(stack).refinements()));
        WeaponDamage.extraHit(target, ElementDamage.of(Element.ANEMO, level.getDamageSources().indirectMagic(target, attacker)), damage);
        WeaponCharge.set(stack, remaining - 1);
        if (remaining - 1 == 0)
            level.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.5F, 1.0F);
        return result;
    }

    private static double speedBonus(int refinement) {
        return SPEED_BASE + SPEED_STEP * (refinement - 1);
    }

    private static double moveBonus(int refinement) {
        return MOVE_BASE + MOVE_STEP * (refinement - 1);
    }

    private static double strikeRatio(int refinement) {
        return STRIKE_BASE + STRIKE_STEP * (refinement - 1);
    }

    private static double powerBonus(int refinement) {
        return POWER_BASE + POWER_STEP * (refinement - 1);
    }
}
