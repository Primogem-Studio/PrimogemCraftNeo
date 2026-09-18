package net.per.primogemcraft.system.weapon;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class WeaponDamage {
    private static final ResourceKey<DamageType> CONTINUOUS_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "continuous_damage"));
    private static final int ATTACK_INTERVAL = 20;

    private WeaponDamage() {
    }

    public static DamageSource continuous(Level level, Element element, Entity direct, Entity causing, ElementDamageOptions options) {
        return ElementDamage.of(element, level.registryAccess().holderOrThrow(CONTINUOUS_DAMAGE), direct, causing, ElementStyle.NORMAL, options);
    }

    public static DamageSource lightning(Level level, Element element, Entity direct, Entity causing, ElementDamageOptions options) {
        return ElementDamage.of(element, level.damageSources().lightningBolt().typeHolder(), direct, causing, ElementStyle.NORMAL, options);
    }

    /**
     * Applies one extra hit and leaves the target with the vanilla attack interval.
     * The target's invulnerability is cleared first so the extra hit lands right after the hit that triggered it,
     * then restored to at least 20 ticks, so no extra damage shortens the interval it is allowed to be hit at.
     */
    public static void extraHit(LivingEntity target, DamageSource source, float damage) {
        var interval = target.invulnerableTime;
        target.invulnerableTime = 0;
        target.hurt(source, damage);
        target.invulnerableTime = Math.max(interval, ATTACK_INTERVAL);
    }
}
