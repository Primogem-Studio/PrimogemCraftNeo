package net.per.primogemcraft.system.element.effect;

import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.per.primogemcraft.compat.event.EventDispatch;
import net.per.primogemcraft.registry.PGCEffects;

public class BurningRecoveryEffect extends StatusEffect {
    private static final float FIRE_HEAL = 2.0F;
    private static final float LAVA_HEAL_RATIO = 0.15F;

    public BurningRecoveryEffect() {
        super(StatusEffectCategory.BENEFICIAL, -1);
    }

    public static void register() {
        EventDispatch.onDamageModify((entity, source, amount) -> {
            if (entity.getWorld().isClient() || !entity.hasStatusEffect(PGCEffects.BURNING_RECOVERY.get())) return amount;
            if (source.isIn(DamageTypeTags.IS_FIRE)) {
                entity.heal(FIRE_HEAL);
                return 0.0F;
            }
            if (source.isOf(DamageTypes.LAVA)) {
                entity.heal(entity.getMaxHealth() * LAVA_HEAL_RATIO);
                return 0.0F;
            }
            return amount;
        });
    }
}
