package net.per.primogemcraft.enchantment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public final class FoolsWrath {
    private static final double CHANCE_PER_LEVEL = 0.02D;
    private static final float DAMAGE_RATIO = 2.0F;

    private FoolsWrath() {
    }

    public static void punish(LivingEntity attacker, LivingEntity victim) {
        if (attacker.level().isClientSide() || attacker == victim) return;
        var level = PGCEnchantments.levelOf(attacker.level(), attacker.getMainHandItem(), PGCEnchantments.FOOLS_WRATH)
                + PGCEnchantments.levelOf(attacker.level(), attacker.getOffhandItem(), PGCEnchantments.FOOLS_WRATH);
        if (level <= 0 || attacker.getRandom().nextDouble() >= level * CHANCE_PER_LEVEL) return;
        attacker.hurt(new DamageSource(attacker.level().holderOrThrow(DamageTypes.MAGIC)), victim.getHealth() * DAMAGE_RATIO);
    }

    public static void cleanse(ServerLevel level, AABB area) {
        for (var item : level.getEntitiesOfClass(ItemEntity.class, area))
            PGCEnchantments.remove(level, item.getItem(), PGCEnchantments.FOOLS_WRATH);
    }
}
