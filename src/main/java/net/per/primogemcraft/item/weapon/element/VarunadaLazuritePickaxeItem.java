package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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

public class VarunadaLazuritePickaxeItem extends WishWeaponToolItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.VARUNADA_LAZURITE_SLIVER, PGCItems.PRIMOGEM);

    private static final float ATTACK_DAMAGE = 4.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DURATION_TICKS = 600.0D;
    private static final double DIG_SPEED_AMPLIFIER = 1.0D;
    private static final double COOLDOWN_TICKS = 2400.0D;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String WATER_TEXT = "water";
    private static final String REFINEMENT_TEXT = "refinement";

    public VarunadaLazuritePickaxeItem(Properties properties) {
        super(TIER, properties.fireResistant(), BlockTags.MINEABLE_WITH_PICKAXE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.HYDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, WATER_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, DURATION_TICKS, true, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!player.isInWater()) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        var duration = ElementWeapons.ticks(player, stack, Element.HYDRO, DURATION_TICKS, true);
        level.playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration,
                ElementWeapons.ticks(player, stack, Element.HYDRO, DIG_SPEED_AMPLIFIER, true), true, false));
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, 0, true, false));
        player.getCooldowns().addCooldown(stack.getItem(), ElementWeapons.ticks(player, stack, Element.HYDRO, COOLDOWN_TICKS, false));
        return InteractionResultHolder.success(stack);
    }
}
