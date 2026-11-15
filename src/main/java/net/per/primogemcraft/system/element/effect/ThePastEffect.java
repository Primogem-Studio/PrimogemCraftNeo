package net.per.primogemcraft.system.element.effect;

import net.per.primogemcraft.compat.attachment.Attachments;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.compat.event.EventDispatch;
import net.per.primogemcraft.registry.PGCAttachments;
import net.per.primogemcraft.util.TemporalRecord;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ThePastEffect extends StatusEffect implements net.per.primogemcraft.compat.event.EffectCallbacks.Started {
    private static final float PLAYER_VOLUME = 0.5F;
    private static final float MOB_VOLUME = 0.1F;
    private static final float START_PITCH = 0.2F;
    private static final float END_VOLUME = 4.0F;
    private static final float END_PITCH = 0.5F;
    private static final float MINIMUM_HEALTH = 1.0F;

    public ThePastEffect() {
        super(StatusEffectCategory.BENEFICIAL, -479233);
    }

    public void onEffectStarted(LivingEntity entity, int amplifier) {
        Attachments.set(entity, PGCAttachments.TEMPORAL_RECORD.get(), new TemporalRecord(entity.getX(), entity.getY(), entity.getZ(), entity.getHealth()));
        var level = entity.getWorld();
        level.playSound(null, entity.getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                entity instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.NEUTRAL,
                entity instanceof PlayerEntity ? PLAYER_VOLUME : MOB_VOLUME, START_PITCH);
    }

    public static void register() {
        EventDispatch.onEffectExpired((entity, instance) -> {
            if (!(instance.getEffectType() instanceof ThePastEffect)) return;
            rewind(entity);
        });
    }

    /**
     * Returns the entity to the position and health recorded when the effect started.
     */
    public static void rewind(LivingEntity entity) {
        var record = Attachments.get(entity, PGCAttachments.TEMPORAL_RECORD.get());
        if (record.health() > 0.0F) {
            entity.requestTeleport(record.x(), record.y(), record.z());
            entity.setHealth(record.health());
        } else {
            entity.setHealth(MINIMUM_HEALTH);
        }
        if (entity instanceof ServerPlayerEntity player) player.closeHandledScreen();
        entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP,
                entity instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.NEUTRAL,
                entity instanceof PlayerEntity ? END_VOLUME : MOB_VOLUME, END_PITCH);
    }
}
