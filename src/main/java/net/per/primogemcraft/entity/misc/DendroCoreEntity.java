package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.world.World;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootContextParameters;
import net.per.primogemcraft.system.element.Element;
import net.per.primogemcraft.system.element.ElementDamage;

public class DendroCoreEntity extends Entity {
    private static final int LIFETIME_TICKS = 100;
    private static final double GRAVITY = 0.04D;
    private static final double DRAG = 0.98D;
    private static final double BOUNCE = 0.4D;
    private static final float BURNING_EXPLOSION_RADIUS = 4.0F;
    private static final float HIT_EXPLOSION_RADIUS = 2.0F;
    private static final String NBT_LIFETIME = "Lifetime";

    private int lifetime;

    public DendroCoreEntity(EntityType<? extends DendroCoreEntity> entityType, World level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (isOnFire()) {
            if (!getWorld().isClient()) {
                detonate(BURNING_EXPLOSION_RADIUS);
                discard();
            }
            return;
        }
        fall();
        if (getWorld().isClient()) return;
        lifetime++;
        if (lifetime < LIFETIME_TICKS) return;
        dropLoot();
        discard();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient() || isRemoved() || isInvulnerableTo(source)) return false;
        if (source.getAttacker() == this) return false;
        if (!isBurningDamage(source)) return false;
        detonate(HIT_EXPLOSION_RADIUS);
        discard();
        return true;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    private static boolean isBurningDamage(DamageSource source) {
        if (source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.LAVA)) return true;
        return ElementDamage.elementOf(source) == Element.PYRO;
    }

    private void detonate(float radius) {
        getWorld().createExplosion(this, getX(), getY(), getZ(), radius, net.minecraft.world.World.ExplosionSourceType.TNT);
    }

    private void fall() {
        setVelocity(getVelocity().add(0.0D, -GRAVITY, 0.0D));
        move(MovementType.SELF, getVelocity());
        var movement = getVelocity();
        var vertical = isOnGround() ? -movement.y * BOUNCE : movement.y * DRAG;
        setVelocity(movement.x * DRAG, vertical, movement.z * DRAG);
    }

    private void dropLoot() {
        if (!(getWorld() instanceof ServerWorld serverLevel)) return;
        var params = new LootContextParameterSet.Builder(serverLevel)
                .add(LootContextParameters.THIS_ENTITY, this)
                .add(LootContextParameters.ORIGIN, getPos())
                .add(LootContextParameters.DAMAGE_SOURCE, serverLevel.getDamageSources().generic())
                .build(LootContextTypes.ENTITY);
        var table = serverLevel.getServer().getLootManager().getLootTable(getType().getLootTableId());
        for (var stack : table.generateLoot(params)) dropStack(stack);
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
        lifetime = compound.getInt(NBT_LIFETIME);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
        compound.putInt(NBT_LIFETIME, lifetime);
    }
}
