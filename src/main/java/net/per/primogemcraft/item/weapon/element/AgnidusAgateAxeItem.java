package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
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

public class AgnidusAgateAxeItem extends WishWeaponAxeItem {
    private static final Tier TIER = new WeaponTier(1561, 10.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.AGNIDUS_AGATE_SLIVER);

    private static final float ATTACK_DAMAGE = 7.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DURATION_TICKS = 100.0D;
    private static final double COOLDOWN_TICKS = 400.0D;
    private static final int REGENERATION_MULTIPLIER = 2;
    private static final int REGENERATION_AMPLIFIER = 2;
    private static final int DURABILITY_LOSS = 5;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 1.0F;
    private static final int LAVA_PARTICLES = 1;
    private static final double LAVA_SPREAD = 1.0D;
    private static final String RIGHT_CLICK = "right_click";
    private static final String RECOVERY_TEXT = "recovery";
    private static final String REFINEMENT_TEXT = "refinement";

    public AgnidusAgateAxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.PYRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, RECOVERY_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, DURATION_TICKS, true, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        var duration = ElementWeapons.ticks(player, stack, Element.PYRO, DURATION_TICKS, true);
        if (player.hasEffect(MobEffects.FIRE_RESISTANCE))
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration * REGENERATION_MULTIPLIER, REGENERATION_AMPLIFIER, true, false));
        else
            player.addEffect(new MobEffectInstance(PGCEffects.BURNING_RECOVERY, duration, 0, false, false));
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.PYRO, COOLDOWN_TICKS, false));
        level.playSound(null, player.blockPosition(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        server.sendParticles(ParticleTypes.LAVA, player.getX(), player.getY(), player.getZ(), LAVA_PARTICLES, 0.0D, LAVA_SPREAD, 0.0D, 0.0D);
        stack.hurtAndBreak(DURABILITY_LOSS, server, player, item -> {
        });
        return InteractionResultHolder.success(stack);
    }
}
