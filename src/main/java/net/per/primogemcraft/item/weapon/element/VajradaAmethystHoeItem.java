package net.per.primogemcraft.item.weapon.element;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
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

public class VajradaAmethystHoeItem extends WishWeaponHoeItem {
    private static final Tier TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.DIAMOND_INCORRECT,
            PGCItems.PRIMOGEM, PGCItems.VAJRADA_AMETHYST_SLIVER);

    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float ATTACK_SPEED = -3.0F;
    private static final double DURABILITY_LOSS = 100.0D;
    private static final double COOLDOWN_TICKS = 1200.0D;
    private static final double RAIN_CHANCE = 0.4D;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_PITCH = 1.0F;
    private static final String RIGHT_CLICK = "right_click";
    private static final String WEATHER_TEXT = "weather";
    private static final String REFINEMENT_TEXT = "refinement";

    public VajradaAmethystHoeItem(Properties properties) {
        super(TIER, properties.fireResistant(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, WEATHER_TEXT,
                WishReports.number(ElementWeapons.ticks(refinement, DURABILITY_LOSS, false, sealed), ChatFormatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), ChatFormatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        var weather = server.getRandom().nextDouble() < RAIN_CHANCE ? "rain" : (server.getRandom().nextDouble() < RAIN_CHANCE ? "thunder" : "clear");
        var source = server.getServer().createCommandSourceStack().withSuppressedOutput();
        server.getServer().getCommands().performPrefixedCommand(source, "weather " + weather);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        player.swing(hand, true);
        var damage = ElementWeapons.ticks(player, stack, Element.ELECTRO, DURABILITY_LOSS, false);
        if (damage > 0) stack.hurtAndBreak(damage, server, player, item -> {
        });
        var cooldown = ElementWeapons.ticks(player, stack, Element.ELECTRO, COOLDOWN_TICKS, false);
        if (cooldown > 0) player.getCooldowns().addCooldown(stack.getItem(), cooldown);
        return InteractionResultHolder.success(stack);
    }
}
