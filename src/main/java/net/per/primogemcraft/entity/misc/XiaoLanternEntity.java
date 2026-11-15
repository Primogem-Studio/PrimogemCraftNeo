package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.world.World;

public class XiaoLanternEntity extends Entity {
    private static final double CULL_HEIGHT = 256.0D;
    private static final double RISE_SPEED = 0.05D;
    private static final float FULL_TURN = 360.0F;

    public XiaoLanternEntity(EntityType<? extends XiaoLanternEntity> entityType, World level) {
        super(entityType, level);
        noClip = true;
        setNoGravity(true);
        setVelocity(0.0D, RISE_SPEED, 0.0D);
        setYaw(random.nextFloat() * FULL_TURN);
        setPitch(random.nextFloat() * FULL_TURN);
    }

    @Override
    public void tick() {
        super.tick();
        setVelocity(0.0D, RISE_SPEED, 0.0D);
        move(MovementType.SELF, getVelocity());
        if (getWorld().isClient()) return;
        if (getY() >= CULL_HEIGHT) discard();
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound compound) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound compound) {
    }
}
