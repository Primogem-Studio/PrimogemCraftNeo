package net.per.primogemcraft.system.weapon;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.per.primogemcraft.registry.PGCEffects;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class TrashCanTenacityEffect extends MobEffect {
    public static final double NEGATE_BASE_CHANCE = 0.04D;
    public static final double NEGATE_CHANCE_PER_LEVEL = 0.01D;
    private static final float SOUND_VOLUME = 0.3F;
    private static final float SOUND_PITCH = 20.0F;

    public TrashCanTenacityEffect() {
        super(MobEffectCategory.BENEFICIAL, -9013642);
    }

    public static double negateChance(int amplifier) {
        return NEGATE_BASE_CHANCE + NEGATE_CHANCE_PER_LEVEL * amplifier;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();
        var instance = entity.getEffect(PGCEffects.TRASH_CAN_TENACITY);
        if (instance == null) return;
        if (entity.getRandom().nextDouble() >= negateChance(instance.getAmplifier())) return;
        event.setCanceled(true);
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
    }
}
