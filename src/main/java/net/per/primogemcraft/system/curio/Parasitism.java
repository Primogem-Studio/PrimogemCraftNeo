package net.per.primogemcraft.system.curio;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.per.primogemcraft.registry.PGCEffects;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public final class Parasitism {
    private static final double BACKLASH = 0.2D;

    private Parasitism() {
    }

    public static void apply(LivingEntity entity, int level, int ticks) {
        entity.addEffect(new MobEffectInstance(PGCEffects.PARASITE.get(), ticks, level - 1, false, true));
    }

    public static void clear(LivingEntity entity) {
        entity.removeEffect(PGCEffects.PARASITE.get());
    }

    public static int level(LivingEntity entity) {
        var instance = entity.getEffect(PGCEffects.PARASITE.get());
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    public static void infectNearby(LivingEntity source, int level, int ticks, double radius, double healthRatio) {
        for (var target : source.level().getEntitiesOfClass(LivingEntity.class, source.getBoundingBox().inflate(radius))) {
            if (target == source || level(target) > 0) continue;
            if (target.getMaxHealth() > source.getMaxHealth() * healthRatio) continue;
            apply(target, level, ticks);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity killer)) return;
        var level = level(killer);
        if (level <= 0) return;
        killer.hurt(killer.damageSources().genericKill(), (float) (killer.getHealth() * level * BACKLASH));
    }
}
