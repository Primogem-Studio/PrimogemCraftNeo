package net.per.primogemcraft.system.curio;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.per.primogemcraft.registry.PGCEffects;

public final class Parasitism {
    private static final double BACKLASH = 0.2D;

    private Parasitism() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(source.getAttacker() instanceof LivingEntity killer)) return;
            var level = level(killer);
            if (level <= 0) return;
            killer.damage(killer.getDamageSources().genericKill(), (float) (killer.getHealth() * level * BACKLASH));
        });
    }

    public static void apply(LivingEntity entity, int level, int ticks) {
        entity.addStatusEffect(new StatusEffectInstance(PGCEffects.PARASITE.get(), ticks, level - 1, false, true));
    }

    public static void clear(LivingEntity entity) {
        entity.removeStatusEffect(PGCEffects.PARASITE.get());
    }

    public static int level(LivingEntity entity) {
        var instance = entity.getStatusEffect(PGCEffects.PARASITE.get());
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    public static void infectNearby(LivingEntity source, int level, int ticks, double radius, double healthRatio) {
        for (var target : source.getWorld().getEntitiesByClass(LivingEntity.class, source.getBoundingBox().expand(radius), t -> true)) {
            if (target == source || level(target) > 0) continue;
            if (target.getMaxHealth() > source.getMaxHealth() * healthRatio) continue;
            apply(target, level, ticks);
        }
    }
}