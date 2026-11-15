package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 10, WeaponTier.DIAMOND_INCORRECT,
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

    public VajradaAmethystHoeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ELECTRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, WEATHER_TEXT,
                WishReports.number(ElementWeapons.ticks(refinement, DURABILITY_LOSS, false, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        var weather = server.getRandom().nextDouble() < RAIN_CHANCE ? "rain" : (server.getRandom().nextDouble() < RAIN_CHANCE ? "thunder" : "clear");
        var source = server.getServer().getCommandSource().withSilent();
        server.getServer().getCommandManager().executeWithPrefix(source, "weather " + weather);
        level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        player.swingHand(hand);
        var damage = ElementWeapons.ticks(player, stack, Element.ELECTRO, DURABILITY_LOSS, false);
        if (damage > 0) stack.damage(damage, player, item -> {
        });
        var cooldown = ElementWeapons.ticks(player, stack, Element.ELECTRO, COOLDOWN_TICKS, false);
        if (cooldown > 0) player.getItemCooldownManager().set(stack.getItem(), cooldown);
        return TypedActionResult.success(stack);
    }
}
