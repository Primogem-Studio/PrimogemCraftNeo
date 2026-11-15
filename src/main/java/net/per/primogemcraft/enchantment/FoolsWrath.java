package net.per.primogemcraft.enchantment;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;

public final class FoolsWrath {
    private static final double CHANCE_PER_LEVEL = 0.02D;
    private static final float DAMAGE_RATIO = 2.0F;

    private FoolsWrath() {
    }

    public static void punish(LivingEntity attacker, LivingEntity victim) {
        if (attacker.getWorld().isClient() || attacker == victim) return;
        var level = PGCEnchantments.levelOf(attacker.getWorld(), attacker.getMainHandStack(), PGCEnchantments.FOOLS_WRATH)
                + PGCEnchantments.levelOf(attacker.getWorld(), attacker.getOffHandStack(), PGCEnchantments.FOOLS_WRATH);
        if (level <= 0 || attacker.getRandom().nextDouble() >= level * CHANCE_PER_LEVEL) return;
        attacker.damage(attacker.getWorld().getDamageSources().magic(), victim.getHealth() * DAMAGE_RATIO);
    }

    public static void cleanse(ServerWorld level, Box area) {
        for (var item : level.getEntitiesByClass(ItemEntity.class, area, e -> true))
            PGCEnchantments.remove(level, item.getStack(), PGCEnchantments.FOOLS_WRATH);
    }
}
