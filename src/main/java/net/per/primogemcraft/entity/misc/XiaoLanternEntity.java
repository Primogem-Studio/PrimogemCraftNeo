package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;

public class XiaoLanternEntity extends Entity {
    private static final double CULL_HEIGHT = 256.0D;
    private static final double RISE_SPEED = 0.05D;
    private static final float FULL_TURN = 360.0F;

    public XiaoLanternEntity(EntityType<? extends XiaoLanternEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        setNoGravity(true);
        setDeltaMovement(0.0D, RISE_SPEED, 0.0D);
        setYRot(random.nextFloat() * FULL_TURN);
        setXRot(random.nextFloat() * FULL_TURN);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, RISE_SPEED, 0.0D);
        move(MoverType.SELF, getDeltaMovement());
        if (level().isClientSide()) return;
        if (getY() >= CULL_HEIGHT) discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
