package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
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

public class VarunadaLazuriteAxeItem extends WishWeaponAxeItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VARUNADA_LAZURITE_SLIVER);

    private static final float ATTACK_DAMAGE = 8.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DURATION_TICKS = 80.0D;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String LEVITATION_TEXT = "levitation";
    private static final String REFINEMENT_TEXT = "refinement";

    public VarunadaLazuriteAxeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.HYDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(NORMAL_ATTACK, LEVITATION_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, DURATION_TICKS, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld server) || !(attacker instanceof PlayerEntity player)) return result;
        var duration = ElementWeapons.ticks(player, stack, Element.HYDRO, DURATION_TICKS, true);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, duration, 0, false, false));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, duration, 0, false, false));
        return result;
    }
}
