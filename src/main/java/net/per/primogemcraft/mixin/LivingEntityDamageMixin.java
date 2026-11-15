package net.per.primogemcraft.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.per.primogemcraft.compat.event.EventDispatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityDamageMixin {

    @ModifyVariable(method = "applyDamage", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float primogemcraft$modifyDamage(float amount, DamageSource source) {
        return EventDispatch.fireDamageModify((LivingEntity) (Object) this, source, amount);
    }
}