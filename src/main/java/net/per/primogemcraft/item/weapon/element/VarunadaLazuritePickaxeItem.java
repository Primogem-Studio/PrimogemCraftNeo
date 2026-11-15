package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.0F, 15, WeaponTier.DIAMOND_INCORRECT,
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

    public VarunadaLazuritePickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.HYDRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, WATER_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, DURATION_TICKS, true, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!player.isTouchingWater()) return TypedActionResult.pass(stack);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        var duration = ElementWeapons.ticks(player, stack, Element.HYDRO, DURATION_TICKS, true);
        level.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundCategory.NEUTRAL, SOUND_VOLUME, SOUND_PITCH);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, duration,
                ElementWeapons.ticks(player, stack, Element.HYDRO, DIG_SPEED_AMPLIFIER, true), true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, duration, 0, true, false));
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.HYDRO, COOLDOWN_TICKS, false));
        return TypedActionResult.success(stack);
    }
}
