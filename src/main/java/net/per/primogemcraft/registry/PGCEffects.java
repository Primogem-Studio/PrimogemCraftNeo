package net.per.primogemcraft.registry;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.system.abundance.AbundanceEffect;
import net.per.primogemcraft.system.curio.effect.*;
import net.per.primogemcraft.system.element.effect.*;
import net.per.primogemcraft.system.tool.effect.AttackBoostEffect;
import net.per.primogemcraft.system.tool.effect.YijiEffect;
import net.per.primogemcraft.system.weapon.TrashCanTenacityEffect;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCEffects {
    public static final DeferredRegister<StatusEffect> REGISTRY = DeferredRegister.create(RegistryKeys.STATUS_EFFECT, MOD_ID);

    private static final double PRESCRIPTION_PENALTY = -0.01D;
    private static final int PRESCRIPTION_FAILURE_COOLDOWN = 12000;
    private static final float PRESCRIPTION_FAILURE_RATIO = 0.5F;

    public static final DeferredHolder<StatusEffect, CurioEffect> PERPETUAL_CUCKOO_CLOCK = REGISTRY.register("perpetual_cuckoo_clock", () -> new CurioEffect(StatusEffectCategory.NEUTRAL, -1));
    public static final DeferredHolder<StatusEffect, CurioEffect> BLACK_FOREST_CUCKOO_CLOCK = REGISTRY.register("black_forest_cuckoo_clock", () -> new CurioEffect(StatusEffectCategory.NEUTRAL, -10066330));
    public static final DeferredHolder<StatusEffect, CurioEffect> INVALID_THOUGHT_CODE_MACHINE = REGISTRY.register("invalid_thought_code_machine", () -> new CurioEffect(StatusEffectCategory.NEUTRAL, -1));
    public static final DeferredHolder<StatusEffect, CurioEffect> WHIMSICAL_FANCIES_MACHINERY_CREW = REGISTRY.register("whimsical_fancies_machinery_crew", () -> new CurioEffect(StatusEffectCategory.NEUTRAL, -1));
    public static final DeferredHolder<StatusEffect, DeathbedEffect> DEATHBED = REGISTRY.register("deathbed", DeathbedEffect::new);
    public static final DeferredHolder<StatusEffect, ThalanToxiFlameEffect> THALAN_TOXI_FLAME = REGISTRY.register("thalan_toxi_flame", ThalanToxiFlameEffect::new);
    public static final DeferredHolder<StatusEffect, ParasiteEffect> PARASITE = REGISTRY.register("parasite", ParasiteEffect::new);
    public static final DeferredHolder<StatusEffect, CurioEffect> FISSION_CUCKOO_CLOCK = REGISTRY.register("fission_cuckoo_clock", () -> new CurioEffect(StatusEffectCategory.HARMFUL, -26113));
    public static final DeferredHolder<StatusEffect, CurioEffect> CUCKOO_CLOCK_TRICK = REGISTRY.register("cuckoo_clock_trick", () -> new CurioEffect(StatusEffectCategory.NEUTRAL, -10066330));
    public static final DeferredHolder<StatusEffect, AtDeathsDoorEffect> AT_DEATHS_DOOR = REGISTRY.register("at_deaths_door", AtDeathsDoorEffect::new);
    public static final DeferredHolder<StatusEffect, YijiEffect> YIJI = REGISTRY.register("yiji", YijiEffect::new);
    public static final DeferredHolder<StatusEffect, LottoPunishmentEffect> LOTTO_PUNISHMENT = REGISTRY.register("lotto_punishment", LottoPunishmentEffect::new);

    public static final DeferredHolder<StatusEffect, ElementalFlightEffect> FLIGHT = REGISTRY.register("flight", ElementalFlightEffect::new);
    public static final DeferredHolder<StatusEffect, HiddenEffect> DAMAGE_ABSORPTION_COOLDOWN = REGISTRY.register("damage_absorption_cooldown", HiddenEffect::new);
    public static final DeferredHolder<StatusEffect, HiddenEffect> RETALIATION_COOLDOWN = REGISTRY.register("retaliation_cooldown", HiddenEffect::new);
    public static final DeferredHolder<StatusEffect, HiddenEffect> PURIFIED_ARMOR_EFFECT_LIMIT = REGISTRY.register("purified_armor_effect_limit", HiddenEffect::new);
    public static final DeferredHolder<StatusEffect, BurningRetaliationEffect> BURNING_RETALIATION = REGISTRY.register("burning_retaliation", BurningRetaliationEffect::new);
    public static final DeferredHolder<StatusEffect, PowderSnowBacklashEffect> POWDER_SNOW_BACKLASH = REGISTRY.register("powder_snow_backlash", PowderSnowBacklashEffect::new);
    public static final DeferredHolder<StatusEffect, PersistentFreezeEffect> PERSISTENT_FREEZE = REGISTRY.register("persistent_freeze", PersistentFreezeEffect::new);
    public static final DeferredHolder<StatusEffect, WarmPowderSnowEffect> WARM_POWDER_SNOW = REGISTRY.register("warm_powder_snow", WarmPowderSnowEffect::new);
    public static final DeferredHolder<StatusEffect, DendroSetEffect> DENDRO_SET = REGISTRY.register("dendro_set", DendroSetEffect::new);
    public static final DeferredHolder<StatusEffect, MagatamaEffect> MAGATAMA = REGISTRY.register("magatama", MagatamaEffect::new);
    public static final DeferredHolder<StatusEffect, ThePastEffect> THE_PAST = REGISTRY.register("the_past", ThePastEffect::new);
    public static final DeferredHolder<StatusEffect, BurningRecoveryEffect> BURNING_RECOVERY = REGISTRY.register("burning_recovery", BurningRecoveryEffect::new);
    public static final DeferredHolder<StatusEffect, ScorchingEffect> SCORCHING = REGISTRY.register("scorching", ScorchingEffect::new);
    public static final DeferredHolder<StatusEffect, CleansingBubbleEffect> CLEANSING_BUBBLE = REGISTRY.register("cleansing_bubble", CleansingBubbleEffect::new);
    public static final DeferredHolder<StatusEffect, HiddenEffect> BUBBLE_LIMIT = REGISTRY.register("bubble_limit", HiddenEffect::new);

    public static final DeferredHolder<StatusEffect, TrashCanTenacityEffect> TRASH_CAN_TENACITY = REGISTRY.register("trash_can_tenacity", TrashCanTenacityEffect::new);

    public static final DeferredHolder<StatusEffect, AttackBoostEffect> ATTACK_BOOST = REGISTRY.register("attack_boost", AttackBoostEffect::new);

    public static final DeferredHolder<StatusEffect, AbundanceEffect> ABUNDANCE = REGISTRY.register("abundance", AbundanceEffect::new);

    public static final DeferredHolder<StatusEffect, PrescriptionEffect> ABSOLUTE_FAILURE_PRESCRIPTION_ZERO = prescription(failurePrescription());
    public static final DeferredHolder<StatusEffect, PrescriptionEffect> ABSOLUTE_FAILURE_PRESCRIPTION_ONE = prescription(reductionPrescription());
    public static final DeferredHolder<StatusEffect, PrescriptionEffect> ABSOLUTE_FAILURE_PRESCRIPTION_TWO = prescription(slownessPrescription());

    public static DeferredHolder<StatusEffect, PrescriptionEffect> prescription(Prescription prescription) {
        return REGISTRY.register(prescription.id(), () -> new PrescriptionEffect(prescription));
    }

    private static Prescription failurePrescription() {
        return new Prescription("absolute_failure_prescription_zero", "prescription.primogemcraft.zero")
                .started(PGCEffects::strike)
                .whenHurt(PRESCRIPTION_FAILURE_COOLDOWN, PGCEffects::strike);
    }

    private static Prescription reductionPrescription() {
        return new Prescription("absolute_failure_prescription_one", "prescription.primogemcraft.one")
                .modifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE)
                .modifier(EntityAttributes.GENERIC_MAX_HEALTH, PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE)
                .modifier(EntityAttributes.GENERIC_ARMOR, PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }

    private static Prescription slownessPrescription() {
        // 1.20.1 没有 BLOCK_BREAK_SPEED / SNEAKING_SPEED / SUBMERGED_MINING_SPEED 属性，
        // 这三项减速无法表达，予以移除；其余减速效果保留。
        return new Prescription("absolute_failure_prescription_two", "prescription.primogemcraft.two")
                .modifier(EntityAttributes.GENERIC_ATTACK_SPEED, PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE)
                .modifier(EntityAttributes.GENERIC_FLYING_SPEED, PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE)
                .modifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE)
                .modifier(net.per.primogemcraft.compat.attribute.AttributesCompat.SWIM_SPEED.get(), PRESCRIPTION_PENALTY, EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }

    private static void strike(ServerPlayerEntity player) {
        player.damage(player.getDamageSources().outOfWorld(), player.getHealth() * PRESCRIPTION_FAILURE_RATIO);
    }
}
