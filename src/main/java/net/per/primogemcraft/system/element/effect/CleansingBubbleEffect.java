package net.per.primogemcraft.system.element.effect;

import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.per.primogemcraft.compat.event.EventDispatch;
import net.per.primogemcraft.collab.genshincraft.GenshinCraftIntegration;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.registry.PGCSounds;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class CleansingBubbleEffect extends StatusEffect implements net.per.primogemcraft.compat.event.EffectCallbacks.Started {
    private static final int LIMIT_DURATION = 40;
    private static final double RADIUS = 4.5D;
    private static final float BURST_HEALTH_RATIO = 0.4F;
    private static final int BURST_DELAY = 20;
    private static final float BURST_VOLUME = 1.0F;
    private static final float BURST_PITCH = 1.0F;

    public CleansingBubbleEffect() {
        super(StatusEffectCategory.NEUTRAL, -3342337);
    }

    public void onEffectStarted(LivingEntity entity, int amplifier) {
        entity.removeStatusEffect(PGCEffects.BUBBLE_LIMIT.get());
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (entity.getWorld().isClient() || !entity.isAlive() || entity.hasStatusEffect(PGCEffects.BUBBLE_LIMIT.get())) return;
        entity.addStatusEffect(new StatusEffectInstance(PGCEffects.BUBBLE_LIMIT.get(), LIMIT_DURATION, 0, false, false));
        entity.heal(amplifier + GenshinCraftIntegration.bubbleHeal());
        return;
    }

    public static void register() {
        EventDispatch.onEffectExpired((entity, instance) -> {
            if (!(instance.getEffectType() instanceof CleansingBubbleEffect)) return;
            if (!(entity.getWorld() instanceof ServerWorld level)) return;
            var server = level.getServer();
            server.execute(() -> burst(level, entity));
        });
    }

    private static void burst(ServerWorld level, LivingEntity entity) {
        var center = entity.getPos();
        for (var target : level.getEntitiesByClass(LivingEntity.class, new Box(center, center).expand(RADIUS), candidate -> candidate != entity))
            target.damage(level.getDamageSources().generic(), entity.getMaxHealth() * BURST_HEALTH_RATIO);
        level.playSound(null, entity.getBlockPos(), PGCSounds.VARUNADA_LAZURITE_BURST.get(), SoundCategory.PLAYERS, BURST_VOLUME, BURST_PITCH);
    }
}
