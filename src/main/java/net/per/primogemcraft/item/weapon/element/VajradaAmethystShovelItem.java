package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
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
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.DIAMOND_INCORRECT,
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

    public VajradaAmethystShovelItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var cooldownBase = PastDelay.secondsOf(stack) * TICKS_PER_SECOND * COOLDOWN_FACTOR;
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(NO_REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE, RECORD_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, cooldownBase, false, sealed), ChatFormatting.AQUA)));
        descriptions.add(WeaponDescription.of(SNEAK_SWING, DELAY_TEXT));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        var ticks = PastDelay.secondsOf(stack) * TICKS_PER_SECOND;
        stack.hurtAndBreak((int) (stack.getMaxDamage() * DURABILITY_RATIO), server, player, item -> {
        });
        var cooldown = ElementWeapons.ticks(player, stack, Element.ELECTRO, ticks * COOLDOWN_FACTOR, false);
        player.getCooldowns().addCooldown(stack.getItem(), cooldown);
        player.addEffect(new MobEffectInstance(PGCEffects.THE_PAST, ticks, 0));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (!entity.level().isClientSide() && entity.isShiftKeyDown()) PastDelay.adjust(entity, stack);
        return super.onEntitySwing(stack, entity, hand);
    }
}
