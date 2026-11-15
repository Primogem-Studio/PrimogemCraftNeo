package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.MathHelper;
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

public class VayudaTurquoisePickaxeItem extends WishWeaponToolItem {
    private static final ToolMaterial TIER = new WeaponTier(1561, 8.5F, 15, WeaponTier.DIAMOND_INCORRECT,
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

    public VayudaTurquoisePickaxeItem(Settings properties) {
        super(TIER, properties.fireproof(), BlockTags.PICKAXE_MINEABLE, ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.ANEMO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(SNEAK_USE, HIGH_FLIGHT_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, SLOW_FALLING_TICKS, true, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!player.isSneaking()) return TypedActionResult.pass(stack);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION,
                ElementWeapons.ticks(player, stack, Element.ANEMO, LEVITATION_TICKS, true), LEVITATION_AMPLIFIER, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING,
                ElementWeapons.ticks(player, stack, Element.ANEMO, SLOW_FALLING_TICKS, true), SLOW_FALLING_AMPLIFIER, false, false));
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.ANEMO, COOLDOWN_TICKS, false));
        player.swingHand(hand);
        level.playSound(null, player.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS,
                RIPTIDE_VOLUME, MathHelper.nextFloat(server.getRandom(), RIPTIDE_PITCH_MIN, RIPTIDE_PITCH_MAX));
        server.spawnParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, player.getX(), player.getY(), player.getZ(),
                SMOKE_COUNT, 0.0D, SMOKE_VERTICAL_SPREAD, 0.0D, SMOKE_SPEED);
        stack.damage(1, player, item -> {
        });
        return TypedActionResult.success(stack);
    }
}
