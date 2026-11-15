package net.per.primogemcraft.item.weapon.element;

import net.minecraft.util.Formatting;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
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
    private static final ToolMaterial TIER = new WeaponTier(1561, 10.0F, 15, WeaponTier.DIAMOND_INCORRECT,
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

    public AgnidusAgateAxeItem(Settings properties) {
        super(TIER, properties.fireproof(), ATTACK_DAMAGE, ATTACK_SPEED);
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), Element.PYRO);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(REFINEMENT_TEXT));
        descriptions.add(WeaponDescription.of(RIGHT_CLICK, RECOVERY_TEXT,
                WishReports.number(ElementWeapons.seconds(refinement, DURATION_TICKS, true, sealed), Formatting.AQUA),
                WishReports.number(ElementWeapons.seconds(refinement, COOLDOWN_TICKS, false, sealed), Formatting.AQUA)));
        return List.copyOf(descriptions);
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (!player.isSneaking()) return TypedActionResult.pass(stack);
        if (!(level instanceof ServerWorld server)) return TypedActionResult.success(stack);
        var duration = ElementWeapons.ticks(player, stack, Element.PYRO, DURATION_TICKS, true);
        if (player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE))
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration * REGENERATION_MULTIPLIER, REGENERATION_AMPLIFIER, true, false));
        else
            player.addStatusEffect(new StatusEffectInstance(PGCEffects.BURNING_RECOVERY.get(), duration, 0, false, false));
        player.getItemCooldownManager().set(stack.getItem(), ElementWeapons.ticks(player, stack, Element.PYRO, COOLDOWN_TICKS, false));
        level.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GHAST_SHOOT, SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        server.spawnParticles(ParticleTypes.LAVA, player.getX(), player.getY(), player.getZ(), LAVA_PARTICLES, 0.0D, LAVA_SPREAD, 0.0D, 0.0D);
        stack.damage(DURABILITY_LOSS, player, item -> {
        });
        return TypedActionResult.success(stack);
    }
}
