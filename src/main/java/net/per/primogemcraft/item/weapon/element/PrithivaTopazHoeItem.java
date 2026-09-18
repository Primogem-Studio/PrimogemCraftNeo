package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
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
    private static final Tier TIER = new WeaponTier(1200, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRITHIVA_TOPAZ_FRAGMENT);

    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final double GEMSTONE_CHANCE = 4.0D;
    private static final double CHANCE_DIVISOR = 1000.0D;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String GEMSTONE_TEXT = "gemstone";
    private static final String REFINEMENT_TEXT = "refinement";

    public PrithivaTopazHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.GEO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(NORMAL_ATTACK, GEMSTONE_TEXT,
                WishReports.percent(ElementWeapons.scaled(refinement, GEMSTONE_CHANCE, true, sealed) / CHANCE_DIVISOR, ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel server) || !(attacker instanceof Player player)) return result;
        var chance = ElementWeapons.scaled(player, stack, Element.GEO, GEMSTONE_CHANCE, true) / CHANCE_DIVISOR;
        if (server.getRandom().nextDouble() >= chance) return result;
        var dropped = new ItemEntity(server, player.getX(), player.getY(), player.getZ(), new ItemStack(PGCItems.PRITHIVA_TOPAZ_GEMSTONE.get()));
        dropped.setPickUpDelay(0);
        dropped.setUnlimitedLifetime();
        server.addFreshEntity(dropped);
        return result;
    }
}
