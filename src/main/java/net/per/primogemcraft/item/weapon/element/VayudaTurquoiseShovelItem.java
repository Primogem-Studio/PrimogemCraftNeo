package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
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

public class VayudaTurquoiseShovelItem extends WishWeaponShovelItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.5F, 14, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.VAYUDA_TURQUOISE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final int DIG_SPEED_DURATION = 100;
    private static final int DIG_SPEED_START_DURATION = 80;
    private static final double DIG_SPEED_THRESHOLD = 200.0D;
    private static final String PASSIVE_ACTION = "passive";
    private static final String MINING_SPEED_TEXT = "mining_speed";
    private static final String REFINEMENT_TEXT = "refinement";

    public VayudaTurquoiseShovelItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ANEMO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(PASSIVE_ACTION, MINING_SPEED_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, DIG_SPEED_THRESHOLD, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        var result = super.postMine(stack, level, state, pos, miningEntity);
        if (!(miningEntity instanceof PlayerEntity player)) return result;
        var current = player.getStatusEffect(StatusEffects.HASTE);
        if (current == null) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, DIG_SPEED_START_DURATION, 0, true, false));
            return result;
        }
        if (current.getDuration() > ElementWeapons.ticks(player, stack, Element.ANEMO, DIG_SPEED_THRESHOLD, true)) {
            player.removeStatusEffect(StatusEffects.HASTE);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, DIG_SPEED_DURATION, 0, true, false));
            return result;
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, current.getDuration(), current.getAmplifier() + 1, false, false));
        return result;
    }
}
