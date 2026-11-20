package net.per.primogemcraft.system.living;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

public final class LivingItemUsePlayer extends FakePlayer {
    private final Player owner;
    private int heldTicks;

    public LivingItemUsePlayer(ServerWorld level, LivingItemEntity entity, Player owner) {
        super(level, new GameProfile(entity.getUuid(), "[LivingItem]"));
        this.owner = owner;
        setItemInHand(Hand.MAIN_HAND, entity.getCarriedStack().copy());
    }

    public Player owner() {
        return owner;
    }

    public void aim(LivingItemEntity entity, LivingEntity target) {
        tickCount = entity.tickCount;
        setPos(entity.getX(), entity.getY() + entity.getBbHeight() * 0.8 - getEyeHeight(), entity.getZ());
        var wishBow = getMainHandItem().getItem() instanceof WishWeaponBowItem;
        var direction = target.getEyePosition().subtract(wishBow ? getEyePosition().add(0.0, -0.1, 0.0) : getEyePosition());
        setYRot((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        setXRot((float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
        if (wishBow) {
            entity.setYRot(getYRot());
            entity.setXRot(getXRot());
        }
        var offset = direction.normalize().scale(entity.getBbWidth() * 0.5D + 0.5D);
        setPos(position().add(offset));
        getAttributes().assignValues(owner.getAttributes());
        removeModifiers(owner.getMainHandStack());
        applyModifiers(getMainHandItem());
    }

    private void removeModifiers(ItemStack stack) {
        for (var entry : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).entries()) {
            var instance = getAttribute(entry.getKey());
            if (instance != null) instance.removeModifier(entry.getValue());
        }
    }

    private void applyModifiers(ItemStack stack) {
        for (var entry : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).entries()) {
            var instance = getAttribute(entry.getKey());
            if (instance == null) continue;
            EntityAttributeModifier modifier = entry.getValue();
            instance.removeModifier(modifier);
            instance.addTemporaryModifier(modifier);
        }
    }

    public void useCarriedItem(boolean start) {
        setItemInHand(Hand.OFF_HAND, owner.getOffHandStack());
        try {
            if (start) {
                var stack = getMainHandItem();
                var result = stack.use(level(), this, Hand.MAIN_HAND);
                if (result.getObject() != stack) setItemInHand(Hand.MAIN_HAND, result.getObject());
                heldTicks = 0;
            } else if (isUsingItem()) {
                var stack = getMainHandItem();
                if (!ForgeHooks.canContinueUsing(getUseItem(), stack)) {
                    stopUsingItem();
                    return;
                }
                useItem = stack;
                updateUsingItem(stack);
                heldTicks++;
                var duration = stack.getMaxUseTime();
                var releaseTicks = stack.getItem() instanceof CrossbowItem ? CrossbowItem.getChargeDuration(stack) + 1
                        : stack.getUseAnimation() == UseAnim.BOW ? 20 : duration > 1200 ? 100 : duration;
                if (isUsingItem() && !(stack.getItem() instanceof WishWeaponBowItem)
                        && (stack.useOnRelease() || duration > 1200) && heldTicks >= releaseTicks) releaseUsingItem();
            }
        } finally {
            owner.setStackInHand(Hand.OFF_HAND, getOffhandItem());
            setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            for (var index = 1; index < getInventory().items.size(); index++) {
                var remainder = getInventory().removeItemNoUpdate(index);
                if (!remainder.isEmpty() && !owner.getInventory().add(remainder)) owner.drop(remainder, false);
            }
        }
    }

    @Override
    public ItemStack getProjectile(ItemStack weapon) {
        return owner.getProjectile(weapon);
    }

    @Override
    public ItemCooldowns getCooldowns() {
        return owner.getCooldowns();
    }
}
