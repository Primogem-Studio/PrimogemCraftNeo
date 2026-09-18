package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class LionsRoarItem extends WishWeaponItem {
    private static final Tier TIER = new WeaponTier(2600, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

    private static final ResourceKey<Enchantment> FIRE_ASPECT = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.withDefaultNamespace("fire_aspect"));
    private static final int MAX_FIRE_STACKS = 6;
    private static final int STRIKE_COOLDOWN = 20;
    private static final double STRIKE_BASE = 0.18D;
    private static final double STRIKE_STEP = 0.045D;
    private static final double IGNITE_BASE = 0.3D;
    private static final double IGNITE_STEP = 0.1D;
    private static final double IGNITE_PER_STACK = 0.05D;
    private static final int BURN_BASE_SECONDS = 8;
    private static final int BURN_STEP_SECONDS = 2;
    private static final double FIRE_DAMAGE_STEP = 0.08D;
    private static final double FIRE_IGNITE_STEP = 0.05D;
    private static final float ATTACK_DAMAGE = 6.5F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String PASSIVE_ACTION = "passive";
    private static final String SPECIAL_ACTION = "special_passive";
    private static final String FIRE_TEXT = "fire";
    private static final String IGNITE_TEXT = "ignite";
    private static final String FIRE_ASPECT_TEXT = "fire_aspect";

    public LionsRoarItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
    }

    @Override
    public List<WeaponModifier> conditionalPassives(Player player, ItemStack stack, int slot, int refinement) {
        var stacks = WeaponCharge.of(stack);
        if (isHeld(player, slot, stack) || stacks <= 0) return List.of();
        return List.of(WeaponModifier.conditional(Attributes.ATTACK_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                (owner, worn, value) -> stacks * FIRE_DAMAGE_STEP));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, FIRE_TEXT,
                        WishReports.percent(STRIKE_BASE + STRIKE_STEP * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, IGNITE_TEXT,
                        WishReports.percent(igniteChance(refinement, 0), ChatFormatting.AQUA),
                        WishReports.number(BURN_BASE_SECONDS + BURN_STEP_SECONDS * (refinement - 1), ChatFormatting.AQUA)),
                WeaponDescription.of(SPECIAL_ACTION, FIRE_ASPECT_TEXT,
                        WishReports.percent(FIRE_IGNITE_STEP, ChatFormatting.RED),
                        WishReports.percent(FIRE_DAMAGE_STEP, ChatFormatting.RED)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.permanent(WeaponCharge.of(stack), MAX_FIRE_STACKS);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) return result;
        var refinement = WeaponState.of(stack).refinements();
        if (target.getRemainingFireTicks() > 0) {
            if (attacker instanceof Player player && player.getCooldowns().isOnCooldown(stack.getItem())) return result;
            WeaponDamage.extraHit(target, WeaponDamage.continuous(level, Element.PYRO, target, attacker, ElementDamageOptions.REACTING),
                    (float) (attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * (STRIKE_BASE + STRIKE_STEP * (refinement - 1))));
            if (attacker instanceof Player player) player.getCooldowns().addCooldown(stack.getItem(), STRIKE_COOLDOWN);
            return result;
        }
        var stacks = WeaponCharge.of(stack);
        if (level.getRandom().nextDouble() >= igniteChance(refinement, stacks)) return result;
        target.igniteForSeconds(BURN_BASE_SECONDS + BURN_STEP_SECONDS * (refinement - 1));
        return result;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide()) return;
        var fire = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(FIRE_ASPECT);
        if (fire.isEmpty()) return;
        var innate = stack.getEnchantmentLevel(fire.get());
        var stacks = WeaponCharge.of(stack);
        if (innate <= 0 || stacks >= MAX_FIRE_STACKS) return;
        WeaponCharge.set(stack, Math.min(MAX_FIRE_STACKS, stacks + innate));
        EnchantmentHelper.updateEnchantments(stack, enchantments -> enchantments.set(fire.get(), 0));
    }

    private static double igniteChance(int refinement, int stacks) {
        return IGNITE_BASE + IGNITE_STEP * (refinement - 1) + IGNITE_PER_STACK * stacks;
    }
}
