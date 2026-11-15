package net.per.primogemcraft.system.weapon;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.per.primogemcraft.registry.PGCEffects;

public class TrashCanTenacityEffect extends StatusEffect {
    public static final double NEGATE_BASE_CHANCE = 0.04D;
    public static final double NEGATE_CHANCE_PER_LEVEL = 0.01D;
    private static final float SOUND_VOLUME = 0.3F;
    private static final float SOUND_PITCH = 20.0F;

    public TrashCanTenacityEffect() {
        super(StatusEffectCategory.BENEFICIAL, -9013642);
    }

    public static double negateChance(int amplifier) {
        return NEGATE_BASE_CHANCE + NEGATE_CHANCE_PER_LEVEL * amplifier;
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            var instance = entity.getStatusEffect(PGCEffects.TRASH_CAN_TENACITY.get());
            if (instance == null) return true;
            if (entity.getRandom().nextDouble() >= negateChance(instance.getAmplifier())) return true;
            entity.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
            return false;
        });
    }
}
