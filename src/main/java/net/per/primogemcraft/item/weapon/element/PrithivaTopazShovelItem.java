package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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

public class PrithivaTopazShovelItem extends WishWeaponShovelItem {
    private static final Tier TIER = new WeaponTier(1890, 9.0F, 14, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.PRITHIVA_TOPAZ_CHUNK);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -2.0F;
    private static final double EXPLOSION_POWER = 4.0D;
    private static final int EXPLOSION_MIN_POWER = 2;
    private static final int MIN_DAMAGE = 10;
    private static final int MAX_DAMAGE = 40;
    private static final double COOLDOWN_TICKS = 40.0D;
    private static final String SNEAK_USE_BLOCK = "sneak_use_block";
    private static final String EXPLOSION_TEXT = "explosion";
    private static final String REFINEMENT_TEXT = "refinement";

    public PrithivaTopazShovelItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.GEO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE_BLOCK, EXPLOSION_TEXT,
                WishReports.number(ElementWeapons.scaled(refinement, EXPLOSION_POWER, true, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        var player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel server) || player == null || !player.isShiftKeyDown()) return result;
        var stack = context.getItemInHand();
        var power = ElementWeapons.scaled(player, stack, Element.GEO, EXPLOSION_POWER, true);
        var pos = context.getClickedPos();
        server.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                Mth.nextInt(server.getRandom(), EXPLOSION_MIN_POWER, (int) (power * 2.0D)), Level.ExplosionInteraction.TNT);
        stack.hurtAndBreak(Mth.nextInt(server.getRandom(), MIN_DAMAGE, MAX_DAMAGE), server, player, item -> {
        });
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.GEO, COOLDOWN_TICKS, false));
        return InteractionResult.SUCCESS;
    }
}
