package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
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

public class VayudaTurquoiseAxeItem extends WishWeaponAxeItem {
    private static final Tier TIER = new WeaponTier(1561, 9.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.VAYUDA_TURQUOISE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 7.5F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int EFFECT_DURATION = 240;
    private static final double SPEED_AMPLIFIER = 0.9D;
    private static final double DURABILITY_LOSS = 4.0D;
    private static final int AMPLIFIER_OFFSET = 1;
    private static final String PASSIVE_ACTION = "passive";
    private static final String SPEED_TEXT = "speed";
    private static final String REFINEMENT_TEXT = "refinement";

    public VayudaTurquoiseAxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ANEMO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, SPEED_TEXT,
                WishReports.number(ElementWeapons.ticks(refinement, SPEED_AMPLIFIER, true, sealed) + AMPLIFIER_OFFSET, ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.ticks(refinement, DURABILITY_LOSS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player) || isHeld(player, slot, stack)) return;
        var effect = player.isInWaterOrBubble() ? MobEffects.DOLPHINS_GRACE : MobEffects.MOVEMENT_SPEED;
        if (player.hasEffect(effect)) return;
        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION,
                ElementWeapons.ticks(player, stack, Element.ANEMO, SPEED_AMPLIFIER, true), false, false));
        var damage = ElementWeapons.ticks(player, stack, Element.ANEMO, DURABILITY_LOSS, false);
        if (damage > 0) stack.hurtAndBreak(damage, server, player, item -> {
        });
    }
}
