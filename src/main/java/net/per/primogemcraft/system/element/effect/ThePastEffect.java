package net.per.primogemcraft.system.element.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.util.TemporalRecord;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class ThePastEffect extends MobEffect {
    private static final float PLAYER_VOLUME = 0.5F;
    private static final float MOB_VOLUME = 0.1F;
    private static final float START_PITCH = 0.2F;
    private static final float END_VOLUME = 4.0F;
    private static final float END_PITCH = 0.5F;
    private static final float MINIMUM_HEALTH = 1.0F;

    public ThePastEffect() {
        super(MobEffectCategory.BENEFICIAL, -479233);
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        entity.setData(PGCAttachments.TEMPORAL_RECORD.get(), new TemporalRecord(entity.getX(), entity.getY(), entity.getZ(), entity.getHealth()));
        var level = entity.level();
        level.playSound(null, entity.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                entity instanceof Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL,
                entity instanceof Player ? PLAYER_VOLUME : MOB_VOLUME, START_PITCH);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (!(event.getEffectInstance().getEffect().value() instanceof ThePastEffect)) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        rewind(entity);
    }

    /**
     * Returns the entity to the position and health recorded when the effect started.
     */
    public static void rewind(LivingEntity entity) {
        var record = entity.getData(PGCAttachments.TEMPORAL_RECORD.get());
        if (record.health() > 0.0F) {
            entity.teleportTo(record.x(), record.y(), record.z());
            entity.setHealth(record.health());
        } else {
            entity.setHealth(MINIMUM_HEALTH);
        }
        if (entity instanceof ServerPlayer player) player.closeContainer();
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                entity instanceof Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL,
                entity instanceof Player ? END_VOLUME : MOB_VOLUME, END_PITCH);
    }
}
