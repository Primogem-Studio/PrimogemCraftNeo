package net.per.primogemcraft.system.element.effect;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.per.primogemcraft.registry.PGCEffects;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class BurningRecoveryEffect extends MobEffect {
    private static final float FIRE_HEAL = 2.0F;
    private static final float LAVA_HEAL_RATIO = 0.15F;

    public BurningRecoveryEffect() {
        super(MobEffectCategory.BENEFICIAL, -1);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingDamageEvent.Pre event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide() || !entity.hasEffect(PGCEffects.BURNING_RECOVERY)) return;
        var source = event.getSource();
        if (source.is(DamageTypes.IN_FIRE)) {
            heal(entity, FIRE_HEAL);
            event.setNewDamage(0.0F);
            return;
        }
        if (source.is(DamageTypes.LAVA)) {
            heal(entity, entity.getMaxHealth() * LAVA_HEAL_RATIO);
            event.setNewDamage(0.0F);
        }
    }

    private static void heal(LivingEntity entity, float amount) {
        entity.heal(amount);
    }
}
