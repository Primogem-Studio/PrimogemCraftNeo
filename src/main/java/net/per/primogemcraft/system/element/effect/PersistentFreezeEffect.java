package net.per.primogemcraft.system.element.effect;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.util.PGCTimer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PersistentFreezeEffect extends StatusEffect {
    private static final String TIMER = "persistent_freeze";
    private static final double ATTACK_SPEED = -0.05D;
    private static final int INTERVAL = 10;
    private static final int INVULNERABLE_TICKS = 10;
    private static final int FROZEN_TICKS = 139;
    private static final int PARTICLES = 5;
    private static final double PARTICLE_SPREAD = 0.3D;
    private static final double PARTICLE_SPEED = 0.05D;

    public PersistentFreezeEffect() {
        super(StatusEffectCategory.HARMFUL, -10027009);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, net.per.primogemcraft.compat.attribute.AttributesCompat.uuid(Identifier.of(MOD_ID, "effect/persistent_freeze")).toString(), ATTACK_SPEED, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (entity.getWorld() instanceof ServerWorld level)
            level.spawnParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), PARTICLES, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPEED);
        if (!entity.hasStatusEffect(PGCEffects.WARM_POWDER_SNOW.get())) entity.setFrozenTicks(FROZEN_TICKS);
        if (PGCTimer.isDone(entity, TIMER) && !entity.hasStatusEffect(PGCEffects.WARM_POWDER_SNOW.get())) {
            PGCTimer.set(entity, TIMER, INTERVAL);
            entity.hurtTime = 0;
            entity.damage(ElementDamage.of(Element.CRYO, entity.getDamageSources().freeze()), amplifier * GenshinCraftIntegration.freezeDamageScale());
            entity.hurtTime = INVULNERABLE_TICKS;
        }
        return;
    }
}
