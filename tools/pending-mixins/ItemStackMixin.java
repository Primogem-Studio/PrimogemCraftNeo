package net.per.primogemcraft.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.enchantment.AmbrosialArborAttachment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    /**
     * 1.21 鐨勭鍚嶆槸 hurtAndBreak(int, ServerWorld, LivingEntity, Consumer<Item>)锛?     * 1.20.1 涓?hurtAndBreak(int, T extends LivingEntity, Consumer<T>)锛屾棤 ServerWorld 鍙傛暟銆?     * 鐩爣涓栫晫鐢卞疄浣撹嚜韬彁渚涖€?     */
    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V", shift = At.Shift.AFTER),
            cancellable = true
    )
    private <T extends LivingEntity> void preventAmbrosialArborBreak(int damage, T entity, Consumer<T> onBreak, CallbackInfo callback) {
        if (damage <= 0 || entity == null) return;
        if (!(entity.level() instanceof ServerWorld level)) return;
        if (AmbrosialArborAttachment.preventBreak(level, entity, (ItemStack) (Object) this)) callback.cancel();
    }
}