package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponHoeItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class PrithivaTopazHoeItem extends WishWeaponHoeItem {
    private static final ToolMaterial TIER = new WeaponTier(1200, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRITHIVA_TOPAZ_FRAGMENT);

    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final double GEMSTONE_CHANCE = 4.0D;
    private static final double CHANCE_DIVISOR = 1000.0D;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String GEMSTONE_TEXT = "gemstone";
    private static final String REFINEMENT_TEXT = "refinement";

    public PrithivaTopazHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.GEO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(NORMAL_ATTACK, GEMSTONE_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, GEMSTONE_CHANCE, true, sealed) / CHANCE_DIVISOR, Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(attacker.getWorld() instanceof ServerWorld server) || !(attacker instanceof PlayerEntity player)) return result;
        var chance = ElementWeapons.scaled(player, stack, Element.GEO, GEMSTONE_CHANCE, true) / CHANCE_DIVISOR;
        if (server.getRandom().nextDouble() >= chance) return result;
        var dropped = new ItemEntity(server, player.getX(), player.getY(), player.getZ(), new ItemStack(PGCItems.PRITHIVA_TOPAZ_GEMSTONE.get()));
        dropped.setPickupDelay(0);
        dropped.setNeverDespawn();
        server.spawnEntity(dropped);
        return result;
    }
}
