package net.per.primogemcraft.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.compat.event.EventDispatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityPickupMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void primogemcraft$onPickup(PlayerEntity player, CallbackInfo ci) {
        EventDispatch.fireItemPickup(player, (ItemEntity) (Object) this);
    }
}