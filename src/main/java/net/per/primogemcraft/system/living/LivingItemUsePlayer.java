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
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

public final class LivingItemUsePlayer extends net.minecraft.server.network.ServerPlayerEntity {
    private final PlayerEntity owner;
    private int heldTicks;

    public LivingItemUsePlayer(ServerWorld level, LivingItemEntity entity, PlayerEntity owner) {
        super(level.getServer(), level, new GameProfile(entity.getUuid(), "[LivingItem]"));
        this.owner = owner;
        setStackInHand(Hand.MAIN_HAND, entity.getCarriedStack().copy());
    }

    public PlayerEntity owner() {
        return owner;
    }

    public void aim(LivingItemEntity entity, LivingEntity target) {
        age = entity.age;
        setPos(entity.getX(), entity.getY() + entity.getHeight() * 0.8 - getStandingEyeHeight(), entity.getZ());
        var wishBow = getMainHandStack().getItem() instanceof WishWeaponBowItem;
        var direction = target.getEyePos().subtract(wishBow ? getEyePos().add(0.0, -0.1, 0.0) : getEyePos());
        setYaw((float) Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        setPitch((float) -Math.toDegrees(Math.atan2(direction.y, direction.horizontalLength())));
        if (wishBow) {
            entity.setYaw(getYaw());
            entity.setPitch(getPitch());
        }
        var offset = direction.normalize().multiply(entity.getWidth() * 0.5D + 0.5D);
        setPos(getPos().add(offset).x, getPos().add(offset).y, getPos().add(offset).z);
        getAttributes().setFrom(owner.getAttributes());
        removeModifiers(owner.getMainHandStack());
        applyModifiers(getMainHandStack());
    }

    private void removeModifiers(ItemStack stack) {
        for (var entry : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).entries()) {
            var instance = getAttributeInstance(entry.getKey());
            if (instance != null) instance.removeModifier(entry.getValue());
        }
    }

    private void applyModifiers(ItemStack stack) {
        for (var entry : stack.getAttributeModifiers(EquipmentSlot.MAINHAND).entries()) {
            var instance = getAttributeInstance(entry.getKey());
            if (instance == null) continue;
            EntityAttributeModifier modifier = entry.getValue();
            instance.removeModifier(modifier);
            instance.addTemporaryModifier(modifier);
        }
    }

    public void useCarriedItem(boolean start) {
        setStackInHand(Hand.OFF_HAND, owner.getOffHandStack());
        try {
            if (start) {
                var stack = getMainHandStack();
                var result = stack.use(getWorld(), this, Hand.MAIN_HAND);
                if (result.getValue() != stack) setStackInHand(Hand.MAIN_HAND, result.getValue());
                heldTicks = 0;
            } else if (isUsingItem()) {
                var stack = getMainHandStack();
                if (!ItemStack.areEqual(getActiveItem(), stack)) {
                    stopUsingItem();
                    return;
                }
                activeItemStack = stack;
                tickItemStackUsage(stack);
                heldTicks++;
                var duration = stack.getMaxUseTime();
                var releaseTicks = stack.getItem() instanceof CrossbowItem ? CrossbowItem.getPullTime(stack) + 1
                        : stack.getUseAction() == UseAction.BOW ? 20 : duration > 1200 ? 100 : duration;
                if (isUsingItem() && !(stack.getItem() instanceof WishWeaponBowItem)
                        && (stack.getItem().isUsedOnRelease(stack) || duration > 1200) && heldTicks >= releaseTicks) stopUsingItem();
            }
        } finally {
            owner.setStackInHand(Hand.OFF_HAND, getOffHandStack());
            setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            for (var index = 1; index < getInventory().main.size(); index++) {
                var remainder = getInventory().removeStack(index);
                if (!remainder.isEmpty() && !owner.getInventory().insertStack(remainder)) owner.dropItem(remainder, false);
            }
        }
    }

    /** 1.20.1 无此覆写点，保留为本类自有方法。 */
    public ItemStack getProjectile(ItemStack weapon) {
        return net.minecraft.item.RangedWeaponItem.getHeldProjectile(owner, weapon.getItem() instanceof net.minecraft.item.RangedWeaponItem ranged ? ranged.getProjectiles() : net.minecraft.item.RangedWeaponItem.BOW_PROJECTILES);
    }

    @Override
    public ItemCooldownManager getItemCooldownManager() {
        return owner.getItemCooldownManager();
    }
}
