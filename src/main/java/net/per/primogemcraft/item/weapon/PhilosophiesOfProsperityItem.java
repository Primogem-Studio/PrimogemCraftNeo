package net.per.primogemcraft.item.weapon;

import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.item.weapon.element.ElementWeapons;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.weapon.WeaponDescription;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WeaponTier;
import net.per.primogemcraft.system.weapon.WishWeaponItem;
import net.per.primogemcraft.system.wish.WishReports;
import net.per.primogemcraft.system.wish.WishTooltips;

import java.util.ArrayList;
import java.util.List;

public class PhilosophiesOfProsperityItem extends WishWeaponItem {
    private static final ToolMaterial TIER = new WeaponTier(801, 4.0F, 5, WeaponTier.STONE_INCORRECT, PGCItems.MORA);

    private static final float ATTACK_DAMAGE = 9.0F;
    private static final float ATTACK_SPEED = -1.0F;
    private static final String NORMAL_ATTACK = "normal_attack";
    private static final String SNEAK_USE = "sneak_use";
    private static final String MAGATAMA_TEXT = "magatama";
    private static final String SCORCHING_TEXT = "scorching";
    private static final String WAX_SEAL_TEXT = "wax_seal";

    private static final double ANEMO_CHANCE = 0.2D;
    private static final double ANEMO_DURATION = 4.0D;
    private static final int ANEMO_AMPLIFIER = 20;
    private static final int ANEMO_SLOW_FALLING_TICKS = 60;
    private static final float ANEMO_SOUND_VOLUME = 0.5F;
    private static final float ANEMO_SOUND_PITCH_MIN = 1.5F;
    private static final float ANEMO_SOUND_PITCH_MAX = 3.0F;
    private static final double GEO_DURATION = 60.0D;
    private static final double GEO_AMPLIFIER = 0.5D;
    private static final double ELECTRO_CHANCE = 0.1D;
    private static final double DENDRO_DURATION = 100.0D;
    private static final double DENDRO_AMPLIFIER = 2.0D;
    private static final double HYDRO_CHANCE = 0.1D;
    private static final double HYDRO_DURATION = 200.0D;
    private static final double HYDRO_AMPLIFIER = 1.0D;
    private static final double PYRO_CHANCE = 0.1D;
    private static final double PYRO_DURATION = 400.0D;
    private static final double PYRO_AMPLIFIER = 1.0D;
    private static final double CRYO_CHANCE = 0.2D;
    private static final double CRYO_DURATION = 200.0D;
    private static final double CRYO_AMPLIFIER = 1.0D;
    private static final float CRYO_SOUND_VOLUME = 0.5F;
    private static final float CRYO_SOUND_PITCH = 1.0F;
    private static final int MAGATAMA_DURATION = 200;
    private static final double MAGATAMA_AMPLIFIER = 1.0D;
    private static final double MAGATAMA_COOLDOWN = 800.0D;
    private static final int MAGATAMA_MIN_COOLDOWN = 100;
    private static final int AMPLIFIER_OFFSET = 1;

    public PhilosophiesOfProsperityItem(Settings properties) {
        super(TIER, ATTACK_DAMAGE, ATTACK_SPEED, properties.fireproof());
    }

    @Override
    protected List<WeaponDescription> description(ItemStack stack) {
        var element = Element.of(stack);
        if (element == null) return List.of(PhilosophyWeapons.moraDescription(stack));
        var sealed = Element.holdsWaxSeal(WishTooltips.viewer(), element);
        var refinement = WeaponState.of(stack).refinements();
        var descriptions = new ArrayList<WeaponDescription>();
        if (sealed) descriptions.add(WeaponDescription.note(WAX_SEAL_TEXT));
        descriptions.add(WeaponDescription.of(NORMAL_ATTACK, element.id(), elementValues(element, refinement, sealed)));
        if (element == Element.ELECTRO) descriptions.add(WeaponDescription.of(SNEAK_USE, MAGATAMA_TEXT,
                WishReports.number(ElementWeapons.ticks(refinement, MAGATAMA_AMPLIFIER, true, sealed) + AMPLIFIER_OFFSET, Formatting.AQUA),
                WishReports.number(magatamaCooldown(refinement, sealed), Formatting.AQUA)));
        if (element == Element.PYRO) descriptions.add(WeaponDescription.note(SCORCHING_TEXT));
        return List.copyOf(descriptions);
    }

    private static Text[] elementValues(Element element, int refinement, boolean sealed) {
        return switch (element) {
            case ANEMO -> chanceValues(refinement, ANEMO_CHANCE, sealed);
            case GEO -> secondValues(refinement, GEO_DURATION, sealed);
            case ELECTRO -> chanceValues(refinement, ELECTRO_CHANCE, sealed);
            case DENDRO -> secondValues(refinement, DENDRO_DURATION, sealed);
            case HYDRO -> new Text[]{
                    WishReports.percent(ElementWeapons.scaled(refinement, HYDRO_CHANCE, true, sealed), Formatting.AQUA),
                    WishReports.number(ElementWeapons.seconds(refinement, HYDRO_DURATION, true, sealed), Formatting.AQUA)};
            case PYRO -> chanceValues(refinement, PYRO_CHANCE, sealed);
            case CRYO -> chanceValues(refinement, CRYO_CHANCE, sealed);
        };
    }

    private static Text[] chanceValues(int refinement, double chance, boolean sealed) {
        return new Text[]{WishReports.percent(ElementWeapons.scaled(refinement, chance, true, sealed), Formatting.AQUA)};
    }

    private static Text[] secondValues(int refinement, double ticks, boolean sealed) {
        return new Text[]{WishReports.number(ElementWeapons.seconds(refinement, ticks, true, sealed), Formatting.AQUA)};
    }

    private static double magatamaCooldown(int refinement, boolean sealed) {
        return Math.max(ElementWeapons.seconds(refinement, MAGATAMA_COOLDOWN, false, sealed),
                MAGATAMA_MIN_COOLDOWN / ElementWeapons.TICKS_PER_SECOND);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.postHit(stack, target, attacker);
        if (!(target.getWorld() instanceof ServerWorld level) || !(attacker instanceof PlayerEntity player)) return result;
        var element = Element.of(stack);
        if (element == null) {
            PhilosophyWeapons.drop(level, target.getPos(), player, stack);
            return result;
        }
        applyElement(level, target, player, stack, element);
        return result;
    }

    @Override
    public TypedActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        var stack = player.getStackInHand(hand);
        if (Element.of(stack) != Element.ELECTRO || !player.isSneaking()) return TypedActionResult.pass(stack);
        if (!(level instanceof ServerWorld)) return TypedActionResult.success(stack);
        player.addStatusEffect(new StatusEffectInstance(PGCEffects.MAGATAMA.get(), MAGATAMA_DURATION,
                ElementWeapons.ticks(player, stack, Element.ELECTRO, MAGATAMA_AMPLIFIER, true)));
        player.getItemCooldownManager().set(stack.getItem(),
                Math.max(ElementWeapons.ticks(player, stack, Element.ELECTRO, MAGATAMA_COOLDOWN, false), MAGATAMA_MIN_COOLDOWN));
        return TypedActionResult.success(stack);
    }

    private static void applyElement(ServerWorld level, LivingEntity target, PlayerEntity attacker, ItemStack stack, Element element) {
        var random = level.getRandom();
        switch (element) {
            case ANEMO -> {
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, ANEMO_CHANCE, true)) return;
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION,
                        ElementWeapons.ticks(attacker, stack, element, ANEMO_DURATION, true), ANEMO_AMPLIFIER, false, false));
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, ANEMO_SLOW_FALLING_TICKS, 0, false, false));
                level.playSound(null, target.getBlockPos(), SoundEvents.ITEM_TRIDENT_RIPTIDE_2, SoundCategory.PLAYERS,
                        ANEMO_SOUND_VOLUME, MathHelper.nextFloat(random, ANEMO_SOUND_PITCH_MIN, ANEMO_SOUND_PITCH_MAX));
            }
            case GEO -> target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,
                    ElementWeapons.ticks(attacker, stack, element, GEO_DURATION, true),
                    ElementWeapons.ticks(attacker, stack, element, GenshinCraftIntegration.geoWeaknessAmplifier(GEO_AMPLIFIER), true), false, false));
            case ELECTRO -> {
                if (attacker.hasStatusEffect(PGCEffects.MAGATAMA.get())) return;
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, ELECTRO_CHANCE, true)) return;
                var bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt == null) return;
                bolt.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(target.getBlockPos()));
                level.spawnEntity(bolt);
            }
            case DENDRO -> {
                var duration = (target.hasStatusEffect(StatusEffects.POISON) ? target.getStatusEffect(StatusEffects.POISON).getDuration() : 0)
                        + ElementWeapons.ticks(attacker, stack, element, DENDRO_DURATION, true);
                var amplifier = (target.hasStatusEffect(StatusEffects.NAUSEA) ? target.getStatusEffect(StatusEffects.NAUSEA).getAmplifier() : 0)
                        - 1 + ElementWeapons.ticks(attacker, stack, element, DENDRO_AMPLIFIER, true);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, duration, amplifier));
            }
            case HYDRO -> {
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, HYDRO_CHANCE, true)) return;
                attacker.addStatusEffect(new StatusEffectInstance(PGCEffects.CLEANSING_BUBBLE.get(),
                        ElementWeapons.ticks(attacker, stack, element, HYDRO_DURATION, true),
                        ElementWeapons.ticks(attacker, stack, element, HYDRO_AMPLIFIER, true)));
            }
            case PYRO -> {
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, PYRO_CHANCE, true)) return;
                target.addStatusEffect(new StatusEffectInstance(PGCEffects.SCORCHING.get(),
                        ElementWeapons.ticks(attacker, stack, element, PYRO_DURATION, true),
                        ElementWeapons.ticks(attacker, stack, element, PYRO_AMPLIFIER, true)));
            }
            case CRYO -> {
                if (target.hasStatusEffect(StatusEffects.SLOWNESS)) return;
                if (random.nextDouble() > ElementWeapons.scaled(attacker, stack, element, CRYO_CHANCE, true)) return;
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,
                        ElementWeapons.ticks(attacker, stack, element, CRYO_DURATION, true),
                        ElementWeapons.ticks(attacker, stack, element, CRYO_AMPLIFIER, true)));
                level.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, CRYO_SOUND_VOLUME, CRYO_SOUND_PITCH);
            }
        }
    }
}
