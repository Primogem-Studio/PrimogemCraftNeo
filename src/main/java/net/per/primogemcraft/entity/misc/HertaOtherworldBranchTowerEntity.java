package net.per.primogemcraft.entity.misc;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.World;
import net.per.primogemcraft.item.misc.EnigmataPetalItem;
import net.per.primogemcraft.system.shop.HertaShop;

public class HertaOtherworldBranchTowerEntity extends Entity {
    public HertaOtherworldBranchTowerEntity(EntityType<? extends HertaOtherworldBranchTowerEntity> entityType, World level) {
        super(entityType, level);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (getWorld().isClient()) return ActionResult.SUCCESS;
        var stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof EnigmataPetalItem petal) {
            if (petal.recall(getWorld(), player, stack)) discard();
            return ActionResult.SUCCESS;
        }
        if (player instanceof ServerPlayerEntity serverPlayer) {
            playSound(SoundEvents.ENTITY_DONKEY_CHEST, 0.4F, MathHelper.nextFloat(random, 0.2F, 1.0F));
            HertaShop.open(serverPlayer);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean isImmuneToExplosion() {
        return true;
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
