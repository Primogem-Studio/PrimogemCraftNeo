package net.per.primogemcraft.system.weapon;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;
import net.per.primogemcraft.system.element.ElementDamageOptions;
import net.per.primogemcraft.system.element.ElementStyle;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class WeaponDamage {
    private static final RegistryKey<DamageType> CONTINUOUS_DAMAGE =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "continuous_damage"));
    private static final int ATTACK_INTERVAL = 20;

    private WeaponDamage() {
    }

    public static DamageSource continuous(World level, Element element, Entity direct, Entity causing, ElementDamageOptions options) {
        return ElementDamage.of(element, level.getRegistryManager().get(net.minecraft.registry.RegistryKeys.DAMAGE_TYPE).getEntry(CONTINUOUS_DAMAGE).orElseThrow(), direct, causing, ElementStyle.NORMAL, options);
    }

    public static DamageSource lightning(World level, Element element, Entity direct, Entity causing, ElementDamageOptions options) {
        return ElementDamage.of(element, level.getDamageSources().lightningBolt(), ElementStyle.NORMAL, options);
    }

    /**
     * Applies one extra hit and leaves the target with the vanilla attack interval.
     * The target's invulnerability is cleared first so the extra hit lands right after the hit that triggered it,
     * then restored to at least 20 ticks, so no extra damage shortens the interval it is allowed to be hit at.
     */
    public static void extraHit(LivingEntity target, DamageSource source, float damage) {
        var interval = target.hurtTime;
        target.hurtTime = 0;
        target.damage(source, damage);
        target.hurtTime = Math.max(interval, ATTACK_INTERVAL);
    }
}
