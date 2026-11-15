package com.tacz.guns.api.event.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * TaCZ API stub for compilation. At runtime the real TaCZ mod provides this class.
 */
public abstract class EntityHurtByGunEvent {
    public abstract Identifier getGunId();
    public abstract Entity getBullet();
    public abstract Entity getHurtEntity();
    public abstract DamageSource getDamageSource(GunDamageSourcePart part);
    public abstract void setDamageSource(GunDamageSourcePart part, DamageSource source);
    public abstract LogicalSide getLogicalSide();

    public static class Pre extends EntityHurtByGunEvent {
        private final Identifier gunId;
        private final Entity bullet;
        private final Entity hurtEntity;
        private DamageSource armorPiercing;
        private DamageSource nonArmorPiercing;
        private final LogicalSide side;

        public Pre(Identifier gunId, Entity bullet, Entity hurtEntity, DamageSource armorPiercing, DamageSource nonArmorPiercing, LogicalSide side) {
            this.gunId = gunId;
            this.bullet = bullet;
            this.hurtEntity = hurtEntity;
            this.armorPiercing = armorPiercing;
            this.nonArmorPiercing = nonArmorPiercing;
            this.side = side;
        }

        @Override public Identifier getGunId() { return gunId; }
        @Override public Entity getBullet() { return bullet; }
        @Override public Entity getHurtEntity() { return hurtEntity; }
        @Override public DamageSource getDamageSource(GunDamageSourcePart part) {
            return part == GunDamageSourcePart.ARMOR_PIERCING ? armorPiercing : nonArmorPiercing;
        }
        @Override public void setDamageSource(GunDamageSourcePart part, DamageSource source) {
            if (part == GunDamageSourcePart.ARMOR_PIERCING) armorPiercing = source;
            else nonArmorPiercing = source;
        }
        @Override public LogicalSide getLogicalSide() { return side; }
    }

    public enum LogicalSide {
        CLIENT, SERVER;
        public boolean isServer() { return this == SERVER; }
    }
}
