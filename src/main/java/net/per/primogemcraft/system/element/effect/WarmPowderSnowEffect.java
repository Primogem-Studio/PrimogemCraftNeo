package net.per.primogemcraft.system.element.effect;

import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.util.PGCTimer;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class WarmPowderSnowEffect extends StatusEffect {
    private static final Identifier FROZEN_TICKS = counter();
    private static final String HEAL_TIMER = "warm_powder_snow_heal";
    private static final String RELEASE_TIMER = "warm_powder_snow_release";
    private static final int HEAL_BASE_INTERVAL = 40;
    private static final int HEAL_AMPLIFIER_REDUCTION = 5;
    private static final int RELEASE_INTERVAL = 200;
    private static final float HEAL_PER_AMPLIFIER = 0.8F;
    private static final int RELEASE_AMPLIFIER_THRESHOLD = 3;
    private static final double FROZEN_TICKS_PER_DAMAGE = 20.0D;
    private static final double FROZEN_TICKS_PER_HEAL = 40.0D;

    public WarmPowderSnowEffect() {
        super(StatusEffectCategory.NEUTRAL, -6684673);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier)
    {
        if (!(entity instanceof PlayerEntity player)) return;
        var flags = PlayerFlags.of(player);
        var frozen = flags.counter(FROZEN_TICKS);
        if (entity.getFrozenTicks() > 1) {
            if (!entity.hasStatusEffect(PGCEffects.PERSISTENT_FREEZE.get())) {
                flags.set(FROZEN_TICKS, frozen + 1);
                if (PGCTimer.isDone(entity, HEAL_TIMER)) {
                    PGCTimer.set(entity, HEAL_TIMER, HEAL_BASE_INTERVAL - amplifier * HEAL_AMPLIFIER_REDUCTION);
                    entity.heal(amplifier * HEAL_PER_AMPLIFIER);
                }
            }
            entity.setFrozenTicks(1);
            return;
        }
        if (frozen <= 0 || !PGCTimer.isDone(entity, RELEASE_TIMER)) return;
        PGCTimer.set(entity, RELEASE_TIMER, RELEASE_INTERVAL);
        flags.set(FROZEN_TICKS, 0);
        if (amplifier < RELEASE_AMPLIFIER_THRESHOLD) {
            var damage = Math.min(entity.getHealth() - 1.0F, frozen / FROZEN_TICKS_PER_DAMAGE);
            if (damage > 0.0F) entity.damage(entity.getDamageSources().freeze(), (float) damage);
            return;
        }
        entity.heal((float) (frozen / FROZEN_TICKS_PER_HEAL * amplifier * 0.5D));
        return;
    }

    private static Identifier counter() {
        return Identifier.of(MOD_ID, "warm_powder_snow_" + "frozen_ticks");
    }
}
