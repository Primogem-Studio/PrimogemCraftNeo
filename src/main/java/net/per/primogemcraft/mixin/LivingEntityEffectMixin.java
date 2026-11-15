package net.per.primogemcraft.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.per.primogemcraft.compat.event.EffectCallbacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityEffectMixin {

    @Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
    private void primogemcraft$effectApplied(StatusEffectInstance instance, net.minecraft.entity.Entity source, CallbackInfo ci) {
        EffectCallbacks.onEffectApplied((LivingEntity) (Object) this, instance);
    }

    @Inject(method = "onStatusEffectUpgraded", at = @At("TAIL"))
    private void primogemcraft$effectUpgraded(StatusEffectInstance instance, boolean reapply, net.minecraft.entity.Entity source, CallbackInfo ci) {
        EffectCallbacks.onEffectApplied((LivingEntity) (Object) this, instance);
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
    private void primogemcraft$effectRemoved(StatusEffectInstance instance, CallbackInfo ci) {
        EffectCallbacks.onStatusEffectRemoved((LivingEntity) (Object) this, instance);
        if (instance.getDuration() <= 0) {
            net.per.primogemcraft.compat.event.EventDispatch.fireEffectExpired((LivingEntity) (Object) this, instance);
        }
    }
}