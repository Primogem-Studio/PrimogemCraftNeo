package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.system.abundance.AbundanceEffect;
import net.per.primogemcraft.system.curio.effect.*;
import net.per.primogemcraft.system.element.effect.*;
import net.per.primogemcraft.system.tool.effect.AttackBoostEffect;
import net.per.primogemcraft.system.tool.effect.YijiEffect;
import net.per.primogemcraft.system.weapon.TrashCanTenacityEffect;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, MOD_ID);

    private static final double PRESCRIPTION_PENALTY = -0.01D;
    private static final int PRESCRIPTION_FAILURE_COOLDOWN = 12000;
    private static final float PRESCRIPTION_FAILURE_RATIO = 0.5F;

    public static final DeferredHolder<MobEffect, CurioEffect> PERPETUAL_CUCKOO_CLOCK = REGISTRY.register("perpetual_cuckoo_clock", () -> new CurioEffect(MobEffectCategory.NEUTRAL, -1));
    public static final DeferredHolder<MobEffect, CurioEffect> BLACK_FOREST_CUCKOO_CLOCK = REGISTRY.register("black_forest_cuckoo_clock", () -> new CurioEffect(MobEffectCategory.NEUTRAL, -10066330));
    public static final DeferredHolder<MobEffect, CurioEffect> INVALID_THOUGHT_CODE_MACHINE = REGISTRY.register("invalid_thought_code_machine", () -> new CurioEffect(MobEffectCategory.NEUTRAL, -1));
    public static final DeferredHolder<MobEffect, CurioEffect> WHIMSICAL_FANCIES_MACHINERY_CREW = REGISTRY.register("whimsical_fancies_machinery_crew", () -> new CurioEffect(MobEffectCategory.NEUTRAL, -1));
    public static final DeferredHolder<MobEffect, DeathbedEffect> DEATHBED = REGISTRY.register("deathbed", DeathbedEffect::new);
    public static final DeferredHolder<MobEffect, ThalanToxiFlameEffect> THALAN_TOXI_FLAME = REGISTRY.register("thalan_toxi_flame", ThalanToxiFlameEffect::new);
    public static final DeferredHolder<MobEffect, ParasiteEffect> PARASITE = REGISTRY.register("parasite", ParasiteEffect::new);
    public static final DeferredHolder<MobEffect, CurioEffect> FISSION_CUCKOO_CLOCK = REGISTRY.register("fission_cuckoo_clock", () -> new CurioEffect(MobEffectCategory.HARMFUL, -26113));
    public static final DeferredHolder<MobEffect, CurioEffect> CUCKOO_CLOCK_TRICK = REGISTRY.register("cuckoo_clock_trick", () -> new CurioEffect(MobEffectCategory.NEUTRAL, -10066330));
    public static final DeferredHolder<MobEffect, AtDeathsDoorEffect> AT_DEATHS_DOOR = REGISTRY.register("at_deaths_door", AtDeathsDoorEffect::new);
    public static final DeferredHolder<MobEffect, YijiEffect> YIJI = REGISTRY.register("yiji", YijiEffect::new);
    public static final DeferredHolder<MobEffect, LottoPunishmentEffect> LOTTO_PUNISHMENT = REGISTRY.register("lotto_punishment", LottoPunishmentEffect::new);

    public static final DeferredHolder<MobEffect, ElementalFlightEffect> FLIGHT = REGISTRY.register("flight", ElementalFlightEffect::new);
    public static final DeferredHolder<MobEffect, HiddenEffect> DAMAGE_ABSORPTION_COOLDOWN = REGISTRY.register("damage_absorption_cooldown", HiddenEffect::new);
    public static final DeferredHolder<MobEffect, HiddenEffect> RETALIATION_COOLDOWN = REGISTRY.register("retaliation_cooldown", HiddenEffect::new);
    public static final DeferredHolder<MobEffect, HiddenEffect> PURIFIED_ARMOR_EFFECT_LIMIT = REGISTRY.register("purified_armor_effect_limit", HiddenEffect::new);
    public static final DeferredHolder<MobEffect, BurningRetaliationEffect> BURNING_RETALIATION = REGISTRY.register("burning_retaliation", BurningRetaliationEffect::new);
    public static final DeferredHolder<MobEffect, PowderSnowBacklashEffect> POWDER_SNOW_BACKLASH = REGISTRY.register("powder_snow_backlash", PowderSnowBacklashEffect::new);
    public static final DeferredHolder<MobEffect, PersistentFreezeEffect> PERSISTENT_FREEZE = REGISTRY.register("persistent_freeze", PersistentFreezeEffect::new);
    public static final DeferredHolder<MobEffect, WarmPowderSnowEffect> WARM_POWDER_SNOW = REGISTRY.register("warm_powder_snow", WarmPowderSnowEffect::new);
    public static final DeferredHolder<MobEffect, DendroSetEffect> DENDRO_SET = REGISTRY.register("dendro_set", DendroSetEffect::new);
    public static final DeferredHolder<MobEffect, MagatamaEffect> MAGATAMA = REGISTRY.register("magatama", MagatamaEffect::new);
    public static final DeferredHolder<MobEffect, ThePastEffect> THE_PAST = REGISTRY.register("the_past", ThePastEffect::new);
    public static final DeferredHolder<MobEffect, BurningRecoveryEffect> BURNING_RECOVERY = REGISTRY.register("burning_recovery", BurningRecoveryEffect::new);
    public static final DeferredHolder<MobEffect, ScorchingEffect> SCORCHING = REGISTRY.register("scorching", ScorchingEffect::new);
    public static final DeferredHolder<MobEffect, CleansingBubbleEffect> CLEANSING_BUBBLE = REGISTRY.register("cleansing_bubble", CleansingBubbleEffect::new);
    public static final DeferredHolder<MobEffect, HiddenEffect> BUBBLE_LIMIT = REGISTRY.register("bubble_limit", HiddenEffect::new);

    public static final DeferredHolder<MobEffect, TrashCanTenacityEffect> TRASH_CAN_TENACITY = REGISTRY.register("trash_can_tenacity", TrashCanTenacityEffect::new);

    public static final DeferredHolder<MobEffect, AttackBoostEffect> ATTACK_BOOST = REGISTRY.register("attack_boost", AttackBoostEffect::new);

    public static final DeferredHolder<MobEffect, AbundanceEffect> ABUNDANCE = REGISTRY.register("abundance", AbundanceEffect::new);

    public static final DeferredHolder<MobEffect, PrescriptionEffect> ABSOLUTE_FAILURE_PRESCRIPTION_ZERO = prescription(failurePrescription());
    public static final DeferredHolder<MobEffect, PrescriptionEffect> ABSOLUTE_FAILURE_PRESCRIPTION_ONE = prescription(reductionPrescription());
    public static final DeferredHolder<MobEffect, PrescriptionEffect> ABSOLUTE_FAILURE_PRESCRIPTION_TWO = prescription(slownessPrescription());

    public static DeferredHolder<MobEffect, PrescriptionEffect> prescription(Prescription prescription) {
        return REGISTRY.register(prescription.id(), () -> new PrescriptionEffect(prescription));
    }

    private static Prescription failurePrescription() {
        return new Prescription("absolute_failure_prescription_zero", "prescription.primogemcraft.zero")
                .started(PGCEffects::strike)
                .whenHurt(PRESCRIPTION_FAILURE_COOLDOWN, PGCEffects::strike);
    }

    private static Prescription reductionPrescription() {
        return new Prescription("absolute_failure_prescription_one", "prescription.primogemcraft.one")
                .modifier(Attributes.ATTACK_DAMAGE, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.MAX_HEALTH, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.ARMOR, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static Prescription slownessPrescription() {
        return new Prescription("absolute_failure_prescription_two", "prescription.primogemcraft.two")
                .modifier(Attributes.ATTACK_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.BLOCK_BREAK_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.FLYING_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.MOVEMENT_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.SNEAKING_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(Attributes.SUBMERGED_MINING_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                .modifier(NeoForgeMod.SWIM_SPEED, PRESCRIPTION_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static void strike(ServerPlayer player) {
        player.hurt(player.damageSources().fellOutOfWorld(), player.getHealth() * PRESCRIPTION_FAILURE_RATIO);
    }
}
