package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.item.misc.EnigmataPetalItem;
import net.per.primogemcraft.system.shop.HertaShop;

public class HertaOtherworldBranchTowerEntity extends Entity {
    public HertaOtherworldBranchTowerEntity(EntityType<? extends HertaOtherworldBranchTowerEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        var stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof EnigmataPetalItem petal) {
            if (petal.recall(level(), player, stack)) discard();
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            playSound(SoundEvents.DONKEY_CHEST, 0.4F, Mth.nextFloat(getRandom(), 0.2F, 1.0F));
            HertaShop.open(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
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
