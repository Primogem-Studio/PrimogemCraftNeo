package net.per.primogemcraft.system.curio.effect;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.per.primogemcraft.util.PGCTimer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PrescriptionEffect extends CurioEffect {
    public static final String NAME_KEY = "effect.primogemcraft.prescription";

    private static final String TIMER_PREFIX = "prescription/";
    private static final int COLOR = -1;

    private final Prescription prescription;

    public PrescriptionEffect(Prescription prescription) {
        super(MobEffectCategory.HARMFUL, COLOR);
        this.prescription = prescription;
        for (var modifier : prescription.modifiers())
            addAttributeModifier(modifier.attribute(), modifierId(modifier.attribute()), modifier.amount(), modifier.operation());
    }

    public Prescription prescription() {
        return prescription;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(NAME_KEY, Component.translatable(prescription.label()));
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (prescription.started() == null || !(entity instanceof ServerPlayer player)) return;
        prescription.started().accept(player);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return prescription.period() != null;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (prescription.period() == null || !(entity instanceof ServerPlayer player)) return true;
        var key = TIMER_PREFIX + prescription.id();
        if (!PGCTimer.isDone(player, key)) return true;
        PGCTimer.set(player, key, prescription.periodTicks());
        prescription.period().accept(player);
        return true;
    }

    @Override
    public void onMobHurt(LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (prescription.hurt() == null || !(entity instanceof ServerPlayer player)) return;
        var key = TIMER_PREFIX + prescription.id() + "/hurt";
        if (!PGCTimer.isDone(player, key)) return;
        PGCTimer.set(player, key, prescription.hurtCooldown());
        prescription.hurt().accept(player);
    }

    private static ResourceLocation modifierId(Holder<Attribute> attribute) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "prescription/" + attribute.unwrapKey().orElseThrow().location().getPath());
    }
}
