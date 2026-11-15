package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.per.primogemcraft.component.PastDelay;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponShovelItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class VajradaAmethystShovelItem extends WishWeaponShovelItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VAJRADA_AMETHYST_SLIVER);

    private static final float ATTACK_DAMAGE = 4.5F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final float DURABILITY_RATIO = 0.02F;
    private static final int TICKS_PER_SECOND = 20;
    private static final double COOLDOWN_FACTOR = 1.5D;
    private static final String SNEAK_USE = "sneak_use";
    private static final String RECORD_TEXT = "record";
    private static final String SNEAK_SWING = "sneak_swing";
    private static final String DELAY_TEXT = "delay";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public VajradaAmethystShovelItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var cooldownBase = PastDelay.secondsOf(stack) * TICKS_PER_SECOND * COOLDOWN_FACTOR;
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE, RECORD_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, cooldownBase, false, sealed), Formatting.AQUA)));
        descriptions.add(WeaponDescription.of(SNEAK_SWING, DELAY_TEXT));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!player.isSneaking()) return TypedActionResult.pass(stack);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        var ticks = PastDelay.secondsOf(stack) * TICKS_PER_SECOND;
        stack.damage((int) (stack.getMaxDamage() * DURABILITY_RATIO), player, item -> {
        });
        var cooldown = ElementWeapons.ticks(player, stack, Element.ELECTRO, ticks * COOLDOWN_FACTOR, false);
        player.getItemCooldownManager().set(stack.getItem(), cooldown);
        player.addStatusEffect(new StatusEffectInstance(PGCEffects.THE_PAST.get(), ticks, 0));
        return TypedActionResult.success(stack);
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!entity.getWorld().isClient() && entity.isSneaking()) PastDelay.adjust(entity, stack);
        return super.onEntitySwing(stack, entity);
    }
}
