package net.per.primogemcraft.system.element.effect;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCSounds;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class CleansingBubbleEffect extends MobEffect {
    private static final int LIMIT_DURATION = 40;
    private static final double RADIUS = 4.5D;
    private static final float BURST_HEALTH_RATIO = 0.4F;
    private static final int BURST_DELAY = 20;
    private static final float BURST_VOLUME = 1.0F;
    private static final float BURST_PITCH = 1.0F;

    public CleansingBubbleEffect() {
        super(MobEffectCategory.NEUTRAL, -3342337);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        entity.removeEffect(PGCEffects.BUBBLE_LIMIT);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide() || !entity.isAlive() || entity.hasEffect(PGCEffects.BUBBLE_LIMIT)) return true;
        entity.addEffect(new MobEffectInstance(PGCEffects.BUBBLE_LIMIT, LIMIT_DURATION, 0, false, false));
        entity.heal(amplifier + GenshinCraftIntegration.bubbleHeal());
        return true;
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEffectInstance().getEffect().value() instanceof CleansingBubbleEffect)) return;
        if (!(event.getEntity() instanceof LivingEntity entity) || !(entity.level() instanceof ServerLevel level)) return;
        var server = level.getServer();
        server.tell(new TickTask(server.getTickCount() + BURST_DELAY, () -> burst(level, entity)));
    }

    private static void burst(ServerLevel level, LivingEntity entity) {
        var center = entity.position();
        for (var target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(RADIUS), candidate -> candidate != entity))
            target.hurt(level.damageSources().generic(), entity.getMaxHealth() * BURST_HEALTH_RATIO);
        level.playSound(null, entity.blockPosition(), PGCSounds.VARUNADA_LAZURITE_BURST.get(), SoundSource.PLAYERS, BURST_VOLUME, BURST_PITCH);
    }
}
