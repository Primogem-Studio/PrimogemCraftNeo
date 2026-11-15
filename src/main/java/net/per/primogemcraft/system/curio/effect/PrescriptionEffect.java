package net.per.primogemcraft.system.curio.effect;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.per.primogemcraft.util.PGCTimer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PrescriptionEffect extends CurioEffect {
    public static final String NAME_KEY = "effect.primogemcraft.prescription";

    private static final String TIMER_PREFIX = "prescription/";
    private static final int COLOR = -1;

    private final Prescription prescription;

    public PrescriptionEffect(Prescription prescription) {
        super(StatusEffectCategory.HARMFUL, COLOR);
        this.prescription = prescription;
        for (var modifier : prescription.modifiers())
            addAttributeModifier(modifier.attribute(), net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(modifierId(modifier.attribute())).toString(), modifier.amount(), modifier.operation());
    }

    public Prescription prescription() {
        return prescription;
    }

    public Text getDisplayName() {
        return Text.translatable(NAME_KEY, Text.translatable(prescription.label()));
    }

    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (prescription.started() == null || !(entity instanceof ServerPlayerEntity player)) return;
        prescription.started().accept(player);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return prescription.period() != null;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (prescription.period() == null || !(entity instanceof ServerPlayerEntity player)) return;
        var key = TIMER_PREFIX + prescription.id();
        if (!PGCTimer.isDone(player, key)) return;
        PGCTimer.set(player, key, prescription.periodTicks());
        prescription.period().accept(player);
        return;
    }

    public void onMobHurt(LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (prescription.damage() == null || !(entity instanceof ServerPlayerEntity player)) return;
        var key = TIMER_PREFIX + prescription.id() + "/hurt";
        if (!PGCTimer.isDone(player, key)) return;
        PGCTimer.set(player, key, prescription.hurtCooldown());
        prescription.damage().accept(player);
    }

    private static Identifier modifierId(EntityAttribute attribute) {
        return Identifier.of(MOD_ID, "prescription/" + net.minecraft.registry.Registries.ATTRIBUTE.getId(attribute).getPath());
    }
}
