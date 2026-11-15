package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.world.World;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.List;

public class LionsRoarItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(2600, 4.0F, 2, WeaponTier.WOODEN_INCORRECT, PGCItems.FINE_ENHANCEMENT_ORE);

    private static final RegistryKey<Enchantment> FIRE_ASPECT = RegistryKey.of(RegistryKeys.ENCHANTMENT, new Identifier("fire_aspect"));
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

    public LionsRoarItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    public List<WeaponModifier> conditionalPassives(PlayerEntity player, ItemStack stack, int slot, int refinement) {
        var stacks = WeaponCharge.of(stack);
        if (isHeld(player, slot, stack) || stacks <= 0) return List.of();
        return List.of(WeaponModifier.conditional(EntityAttributes.GENERIC_ATTACK_DAMAGE, EntityAttributeModifier.Operation.MULTIPLY_BASE,
                (owner, worn, value) -> stacks * FIRE_DAMAGE_STEP));
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var refinement = WeaponState.of(stack).refinements();
        return List.of(
                WeaponDescription.of(NORMAL_ATTACK, FIRE_TEXT,
                        WishReports.percent(STRIKE_BASE + STRIKE_STEP * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.of(PASSIVE_ACTION, IGNITE_TEXT,
                        WishReports.percent(igniteChance(refinement, 0), Formatting.AQUA),
                        WishReports.number(BURN_BASE_SECONDS + BURN_STEP_SECONDS * (refinement - 1), Formatting.AQUA)),
                WeaponDescription.of(SPECIAL_ACTION, FIRE_ASPECT_TEXT,
                        WishReports.percent(FIRE_IGNITE_STEP, Formatting.RED),
                        WishReports.percent(FIRE_DAMAGE_STEP, Formatting.RED)));
    }

    @Override
    public WeaponStacks stacks(ItemStack stack) {
        return WeaponStacks.permanent(WeaponCharge.of(stack), MAX_FIRE_STACKS);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld level)) return result;
        var refinement = WeaponState.of(stack).refinements();
        if (target.getFireTicks() > 0) {
            if (attacker instanceof PlayerEntity player && player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
            WeaponDamage.extraHit(target, WeaponDamage.continuous(level, Element.PYRO, target, attacker, ElementDamageOptions.REACTING),
                    (float) (attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) * (STRIKE_BASE + STRIKE_STEP * (refinement - 1))));
            if (attacker instanceof PlayerEntity player) player.getItemCooldownManager().set(stack.getItem(), STRIKE_COOLDOWN);
            return result;
        }
        var stacks = WeaponCharge.of(stack);
        if (level.getRandom().nextDouble() >= igniteChance(refinement, stacks)) return result;
        target.setOnFireFor((int) (BURN_BASE_SECONDS + BURN_STEP_SECONDS * (refinement - 1)));
        return result;
    }

    @Override
    public void inventoryTick(ItemStack stack, World level, net.minecraft.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClient()) return;
        var fire = level.getRegistryManager().get(RegistryKeys.ENCHANTMENT).get(FIRE_ASPECT);
        if (fire == null) return;
        var innate = net.per.primogemcraft.compat.item.Enchantments120.level(stack, fire);
        var stacks = WeaponCharge.of(stack);
        if (innate <= 0 || stacks >= MAX_FIRE_STACKS) return;
        WeaponCharge.set(stack, Math.min(MAX_FIRE_STACKS, stacks + innate));
        net.per.primogemcraft.compat.item.Enchantments120.update(stack, enchantments -> enchantments.set(fire, 0));
    }

    private static double igniteChance(int refinement, int stacks) {
        return IGNITE_BASE + IGNITE_STEP * (refinement - 1) + IGNITE_PER_STACK * stacks;
    }
}
