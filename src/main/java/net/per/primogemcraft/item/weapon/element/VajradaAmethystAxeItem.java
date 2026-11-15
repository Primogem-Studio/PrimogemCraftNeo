package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.component.PastDelay;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponAxeItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class VajradaAmethystAxeItem extends WishWeaponAxeItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT, PGCItems.PRIMOGEM, PGCItems.VAJRADA_AMETHYST_SLIVER);

    private static final float ATTACK_DAMAGE = 6.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int TICKS_PER_SECOND = 20;
    private static final double COOLDOWN_FACTOR = 0.5D;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String RECORD_TEXT = "record";
    private static final String SNEAK_SWING = "sneak_swing";
    private static final String DELAY_TEXT = "delay";
    private static final String NO_REFINEMENT_TEXT = "no_refinement";

    public VajradaAmethystAxeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var cooldownBase = PastDelay.secondsOf(stack) * TICKS_PER_SECOND * COOLDOWN_FACTOR;
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(NORMAL_ATTACK, RECORD_TEXT, WishReports.number(ElementWeapons.seconds(refinement, cooldownBase, false, sealed), Formatting.AQUA)));
        descriptions.add(WeaponDescription.of(SNEAK_SWING, DELAY_TEXT));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (attacker.getWorld().isClient || !(attacker instanceof PlayerEntity player)) return result;
        if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) return result;
        var ticks = PastDelay.secondsOf(stack) * TICKS_PER_SECOND;
        target.addStatusEffect(new StatusEffectInstance(PGCEffects.THE_PAST.get(), ticks, 0));
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.ELECTRO, ticks * COOLDOWN_FACTOR, false));
        return result;
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!entity.getWorld().isClient() && entity.isSneaking()) PastDelay.adjust(entity, stack);
        return super.onEntitySwing(stack, entity);
    }
}
