package net.per.primogemcraft.system.living;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.per.primogemcraft.entity.mob.LivingItemEntity;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

public final class LivingItemUsePlayer extends FakePlayer {
    private final Player owner;
    private int heldTicks;

    public LivingItemUsePlayer(ServerLevel level, LivingItemEntity entity, Player owner) {
        super(level, new GameProfile(entity.getUUID(), "[LivingItem]"));
        this.owner = owner;
        setItemInHand(InteractionHand.MAIN_HAND, entity.getCarriedStack().copy());
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
        getAttributes().assignAllValues(owner.getAttributes());
        owner.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            var instance = getAttribute(attribute);
            if (instance != null) instance.removeModifier(modifier.id());
        });
        getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            var instance = getAttribute(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.id());
                instance.addTransientModifier(modifier);
            }
        });
    }

    public void useCarriedItem(boolean start) {
        setItemInHand(InteractionHand.OFF_HAND, owner.getOffhandItem());
        try {
            if (start) {
                var stack = getMainHandItem();
                var result = stack.use(level(), this, InteractionHand.MAIN_HAND);
                if (result.getObject() != stack) setItemInHand(InteractionHand.MAIN_HAND, result.getObject());
                heldTicks = 0;
            } else if (isUsingItem()) {
                var stack = getMainHandItem();
                if (!CommonHooks.canContinueUsing(getUseItem(), stack)) {
                    stopUsingItem();
                    return;
                }
                useItem = stack;
                updateUsingItem(stack);
                heldTicks++;
                var duration = stack.getUseDuration(this);
                var releaseTicks = stack.getItem() instanceof CrossbowItem ? CrossbowItem.getChargeDuration(stack, this) + 1
                        : stack.getUseAnimation() == UseAnim.BOW ? 20 : duration > 1200 ? 100 : duration;
                if (isUsingItem() && !(stack.getItem() instanceof WishWeaponBowItem)
                        && (stack.useOnRelease() || duration > 1200) && heldTicks >= releaseTicks) releaseUsingItem();
            }
        } finally {
            owner.setItemInHand(InteractionHand.OFF_HAND, getOffhandItem());
            setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
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
