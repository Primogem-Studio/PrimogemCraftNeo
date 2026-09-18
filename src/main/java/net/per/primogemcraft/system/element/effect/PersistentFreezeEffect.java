package net.per.primogemcraft.system.element.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.util.PGCTimer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PersistentFreezeEffect extends MobEffect {
    private static final String TIMER = "persistent_freeze";
    private static final double ATTACK_SPEED = -0.05D;
    private static final int INTERVAL = 10;
    private static final int INVULNERABLE_TICKS = 10;
    private static final int FROZEN_TICKS = 139;
    private static final int PARTICLES = 5;
    private static final double PARTICLE_SPREAD = 0.3D;
    private static final double PARTICLE_SPEED = 0.05D;

    public PersistentFreezeEffect() {
        super(MobEffectCategory.HARMFUL, -10027009);
        addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath(MOD_ID, "effect/persistent_freeze"), ATTACK_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel level)
            level.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), PARTICLES, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        if (!entity.hasEffect(PGCEffects.WARM_POWDER_SNOW)) entity.setTicksFrozen(FROZEN_TICKS);
        if (PGCTimer.isDone(entity, TIMER) && !entity.hasEffect(PGCEffects.WARM_POWDER_SNOW)) {
            PGCTimer.set(entity, TIMER, INTERVAL);
            entity.invulnerableTime = 0;
            entity.hurt(ElementDamage.of(Element.CRYO, entity.damageSources().freeze()), amplifier * GenshinCraftIntegration.freezeDamageScale());
            entity.invulnerableTime = INVULNERABLE_TICKS;
        }
        return true;
    }
}
