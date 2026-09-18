package net.per.primogemcraft.item.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
    private static final Tier TIER = new WeaponTier(801, 4.0F, 5, WeaponTier.STONE_INCORRECT, PGCItems.MORA);

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

    public PhilosophiesOfProsperityItem(Properties properties) {
        super(TIER, properties.attributes(SwordItem.createAttributes(TIER, ATTACK_DAMAGE, ATTACK_SPEED)).fireResistant());
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
                WishReports.number(ElementWeapons.ticks(refinement, MAGATAMA_AMPLIFIER, true, sealed) + AMPLIFIER_OFFSET, ChatFormatting.AQUA),
                WishReports.number(magatamaCooldown(refinement, sealed), ChatFormatting.AQUA)));
        if (element == Element.PYRO) descriptions.add(WeaponDescription.note(SCORCHING_TEXT));
        return List.copyOf(descriptions);
    }

    private static Component[] elementValues(Element element, int refinement, boolean sealed) {
        return switch (element) {
            case ANEMO -> chanceValues(refinement, ANEMO_CHANCE, sealed);
            case GEO -> secondValues(refinement, GEO_DURATION, sealed);
            case ELECTRO -> chanceValues(refinement, ELECTRO_CHANCE, sealed);
            case DENDRO -> secondValues(refinement, DENDRO_DURATION, sealed);
            case HYDRO -> new Component[]{
                    WishReports.percent(ElementWeapons.scaled(refinement, HYDRO_CHANCE, true, sealed), ChatFormatting.AQUA),
                    WishReports.number(ElementWeapons.seconds(refinement, HYDRO_DURATION, true, sealed), ChatFormatting.AQUA)};
            case PYRO -> chanceValues(refinement, PYRO_CHANCE, sealed);
            case CRYO -> chanceValues(refinement, CRYO_CHANCE, sealed);
        };
    }

    private static Component[] chanceValues(int refinement, double chance, boolean sealed) {
        return new Component[]{WishReports.percent(ElementWeapons.scaled(refinement, chance, true, sealed), ChatFormatting.AQUA)};
    }

    private static Component[] secondValues(int refinement, double ticks, boolean sealed) {
        return new Component[]{WishReports.number(ElementWeapons.seconds(refinement, ticks, true, sealed), ChatFormatting.AQUA)};
    }

    private static double magatamaCooldown(int refinement, boolean sealed) {
        return Math.max(ElementWeapons.seconds(refinement, MAGATAMA_COOLDOWN, false, sealed),
                MAGATAMA_MIN_COOLDOWN / ElementWeapons.TICKS_PER_SECOND);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        var result = super.hurtEnemy(stack, target, attacker);
        if (!(target.level() instanceof ServerLevel level) || !(attacker instanceof Player player)) return result;
        var element = Element.of(stack);
        if (element == null) {
            PhilosophyWeapons.drop(level, target.position(), player, stack);
            return result;
        }
        applyElement(level, target, player, stack, element);
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (Element.of(stack) != Element.ELECTRO || !player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (!(level instanceof ServerLevel)) return InteractionResultHolder.success(stack);
        player.addEffect(new MobEffectInstance(PGCEffects.MAGATAMA, MAGATAMA_DURATION,
                ElementWeapons.ticks(player, stack, Element.ELECTRO, MAGATAMA_AMPLIFIER, true)));
        player.getCooldowns().addCooldown(stack.getItem(),
                Math.max(ElementWeapons.ticks(player, stack, Element.ELECTRO, MAGATAMA_COOLDOWN, false), MAGATAMA_MIN_COOLDOWN));
        return InteractionResultHolder.success(stack);
    }

    private static void applyElement(ServerLevel level, LivingEntity target, Player attacker, ItemStack stack, Element element) {
        var random = level.getRandom();
        switch (element) {
            case ANEMO -> {
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, ANEMO_CHANCE, true)) return;
                target.addEffect(new MobEffectInstance(MobEffects.LEVITATION,
                        ElementWeapons.ticks(attacker, stack, element, ANEMO_DURATION, true), ANEMO_AMPLIFIER, false, false));
                target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, ANEMO_SLOW_FALLING_TICKS, 0, false, false));
                level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS,
                        ANEMO_SOUND_VOLUME, Mth.nextFloat(random, ANEMO_SOUND_PITCH_MIN, ANEMO_SOUND_PITCH_MAX));
            }
            case GEO -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    ElementWeapons.ticks(attacker, stack, element, GEO_DURATION, true),
                    ElementWeapons.ticks(attacker, stack, element, GenshinCraftIntegration.geoWeaknessAmplifier(GEO_AMPLIFIER), true), false, false));
            case ELECTRO -> {
                if (attacker.hasEffect(PGCEffects.MAGATAMA)) return;
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, ELECTRO_CHANCE, true)) return;
                var bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt == null) return;
                bolt.moveTo(Vec3.atBottomCenterOf(target.blockPosition()));
                level.addFreshEntity(bolt);
            }
            case DENDRO -> {
                var duration = (target.hasEffect(MobEffects.POISON) ? target.getEffect(MobEffects.POISON).getDuration() : 0)
                        + ElementWeapons.ticks(attacker, stack, element, DENDRO_DURATION, true);
                var amplifier = (target.hasEffect(MobEffects.CONFUSION) ? target.getEffect(MobEffects.CONFUSION).getAmplifier() : 0)
                        - 1 + ElementWeapons.ticks(attacker, stack, element, DENDRO_AMPLIFIER, true);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier));
            }
            case HYDRO -> {
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, HYDRO_CHANCE, true)) return;
                attacker.addEffect(new MobEffectInstance(PGCEffects.CLEANSING_BUBBLE,
                        ElementWeapons.ticks(attacker, stack, element, HYDRO_DURATION, true),
                        ElementWeapons.ticks(attacker, stack, element, HYDRO_AMPLIFIER, true)));
            }
            case PYRO -> {
                if (random.nextDouble() >= ElementWeapons.scaled(attacker, stack, element, PYRO_CHANCE, true)) return;
                target.addEffect(new MobEffectInstance(PGCEffects.SCORCHING,
                        ElementWeapons.ticks(attacker, stack, element, PYRO_DURATION, true),
                        ElementWeapons.ticks(attacker, stack, element, PYRO_AMPLIFIER, true)));
            }
            case CRYO -> {
                if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) return;
                if (random.nextDouble() > ElementWeapons.scaled(attacker, stack, element, CRYO_CHANCE, true)) return;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        ElementWeapons.ticks(attacker, stack, element, CRYO_DURATION, true),
                        ElementWeapons.ticks(attacker, stack, element, CRYO_AMPLIFIER, true)));
                level.playSound(null, target.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, CRYO_SOUND_VOLUME, CRYO_SOUND_PITCH);
            }
        }
    }
}
