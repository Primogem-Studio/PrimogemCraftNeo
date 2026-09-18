package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponToolItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class VayudaTurquoisePickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(1561, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.VAYUDA_TURQUOISE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 5.0F;
    private static final float ATTACK_SPEED = -2.8F;
    private static final double LEVITATION_TICKS = 2.0D;
    private static final int LEVITATION_AMPLIFIER = 127;
    private static final double SLOW_FALLING_TICKS = 100.0D;
    private static final int SLOW_FALLING_AMPLIFIER = 2;
    private static final double COOLDOWN_TICKS = 400.0D;
    private static final float RIPTIDE_VOLUME = 0.3F;
    private static final float RIPTIDE_PITCH_MIN = 1.2F;
    private static final float RIPTIDE_PITCH_MAX = 5.0F;
    private static final int SMOKE_COUNT = 30;
    private static final double SMOKE_VERTICAL_SPREAD = 4.0D;
    private static final double SMOKE_SPEED = 0.8D;
    private static final String SNEAK_USE = "sneak_use";
    private static final String HIGH_FLIGHT_TEXT = "high_flight";
    private static final String REFINEMENT_TEXT = "refinement";

    public VayudaTurquoisePickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ANEMO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE, HIGH_FLIGHT_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, SLOW_FALLING_TICKS, true, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION,
                ElementWeapons.ticks(player, stack, Element.ANEMO, LEVITATION_TICKS, true), LEVITATION_AMPLIFIER, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,
                ElementWeapons.ticks(player, stack, Element.ANEMO, SLOW_FALLING_TICKS, true), SLOW_FALLING_AMPLIFIER, false, false));
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.ANEMO, COOLDOWN_TICKS, false));
        player.swing(hand, true);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS,
                RIPTIDE_VOLUME, Mth.nextFloat(server.getRandom(), RIPTIDE_PITCH_MIN, RIPTIDE_PITCH_MAX));
        server.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, player.getX(), player.getY(), player.getZ(),
                SMOKE_COUNT, 0.0D, SMOKE_VERTICAL_SPREAD, 0.0D, SMOKE_SPEED);
        stack.hurtAndBreak(1, server, player, item -> {
        });
        return InteractionResultHolder.success(stack);
    }
}
