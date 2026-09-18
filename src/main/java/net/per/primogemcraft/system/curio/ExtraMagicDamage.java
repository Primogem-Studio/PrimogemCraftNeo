package net.per.primogemcraft.system.curio;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class ExtraMagicDamage {
    private static final String GATE = "curio/extra_magic_damage";
    private static final int GATE_TICKS = 20;
    private static final double DAMAGE_PER_CURIO = 0.003D;
    private static final double DAMAGE_LIMIT = 0.3D;

    private ExtraMagicDamage() {
    }

    public static void strike(CurioContext context, LivingEntity victim) {
        var player = context.player();
        var ratio = Math.min(DAMAGE_LIMIT, DAMAGE_PER_CURIO * Curios.damagedCount(player));
        if (ratio <= 0.0D) return;
        if (!context.ready(GATE, GATE_TICKS)) return;
        var invulnerable = victim.invulnerableTime;
        victim.invulnerableTime = 0;
        victim.hurt(new DamageSource(context.level().damageSources().magic().typeHolder(), player), (float) (player.getMaxHealth() * ratio));
        victim.invulnerableTime = invulnerable;
    }
}
