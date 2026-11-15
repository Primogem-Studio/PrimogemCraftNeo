package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class FavoniusLanceItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(960, 4.0F, 10, WeaponTier.WOODEN_INCORRECT, PGCItems.VAYUDA_TURQUOISE_SLIVER);

    private static final double CHANCE_BASE = 0.6D;
    private static final double CHANCE_STEP = 0.1D;
    private static final int FOOD_BASE = 6;
    private static final int FOOD_STEP = 1;
    private static final double SATURATION_BASE = 0.3D;
    private static final double SATURATION_STEP = 0.1D;
    private static final double COOLDOWN_BASE = 240.0D;
    private static final double COOLDOWN_STEP = 30.0D;
    private static final double MAX_SATURATION = 20.0D;
    private static final int MIN_COOLDOWN = 20;
    private static final double HEALTH_BASE = 0.1D;
    private static final double HEALTH_STEP = 0.025D;
    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String RESTORE_TEXT = "restore";
    private static final String COOLDOWN_TEXT = "cooldown";
    private static final String HEALTH_TEXT = "health";

    public FavoniusLanceItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), WeaponModifier.of(EntityAttributes.GENERIC_MAX_HEALTH, HEALTH_BASE, HEALTH_STEP, EntityAttributeModifier.Operation.MULTIPLY_BASE));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, RESTORE_TEXT,
                        WishReports.percent(chance(refinement), Formatting.AQUA),
                        WishReports.number(food(refinement), Formatting.AQUA),
                        WishReports.number(saturation(refinement), Formatting.AQUA),
                        WishReports.number(food(refinement), Formatting.AQUA)),
                WeaponDescription.note(COOLDOWN_TEXT,
                        WishReports.number(cooldownSeconds(refinement), Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, HEALTH_TEXT,
                        WishReports.percent(HEALTH_BASE + HEALTH_STEP * (refinement - 1), Formatting.AQUA)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld level) || !(attacker instanceof PlayerEntity player)) return result;
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
        var refinement = WeaponState.of(stack).refinements();
        if (level.getRandom().nextDouble() >= chance(refinement)) return result;
        var food = player.getHungerManager();
        food.setFoodLevel(food.getFoodLevel() + food(refinement));
        food.setSaturationLevel((float) Math.min(MAX_SATURATION, food.getSaturationLevel() + saturation(refinement)));
        player.heal(food(refinement));
        player.getItemCooldownManager().set(stack.getItem(), cooldownTicks(refinement));
        return result;
    }

    private static double chance(int refinement) {
        return CHANCE_BASE + CHANCE_STEP * (refinement - 1);
    }

    private static int food(int refinement) {
        return FOOD_BASE + FOOD_STEP * (refinement - 1);
    }

    private static double saturation(int refinement) {
        return SATURATION_BASE + SATURATION_STEP * (refinement - 1);
    }

    private static int cooldownTicks(int refinement) {
        return (int) Math.max(MIN_COOLDOWN, COOLDOWN_BASE - COOLDOWN_STEP * (refinement - 1));
    }

    private static double cooldownSeconds(int refinement) {
        return cooldownTicks(refinement) / 20.0D;
    }
}
