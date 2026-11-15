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

public class TheBlackSwordItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(1500, 5.0F, 20, WeaponTier.WOODEN_INCORRECT, PGCItems.PRITHIVA_TOPAZ_FRAGMENT);

    private static final WeaponModifier PASSIVE = WeaponModifier.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.2D, 0.05D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
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

    public TheBlackSwordItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof(), PASSIVE);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld() instanceof ServerWorld level
                && !(attacker instanceof PlayerEntity player && player.getItemCooldownManager().isCoolingDown(stack.getItem()))
                && level.getRandom().nextDouble() >= HEAL_CHANCE) {
            attacker.heal((float) (attacker.getMaxHealth() * healRatio(WeaponState.of(stack).refinements())));
            if (attacker instanceof PlayerEntity player) player.getItemCooldownManager().set(stack.getItem(), COOLDOWN_TICKS);
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refinement = state.refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, HEAL_TEXT,
                        WishReports.percent(healRatio(refinement), Formatting.AQUA),
                        WishReports.number(COOLDOWN_SECONDS, Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, ATTACK_TEXT,
                        WishReports.percent(PASSIVE.amount(refinement), Formatting.AQUA)));
    }

    private static double healRatio(int refinement) {
        return HEAL_BASE + HEAL_STEP * (refinement - 1);
    }
}
