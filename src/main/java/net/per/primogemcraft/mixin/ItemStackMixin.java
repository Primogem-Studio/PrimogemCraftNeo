package net.per.primogemcraft.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.enchantment.AmbrosialArborAttachment;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setDamageValue(I)V", shift = At.Shift.AFTER),
            cancellable = true
    )
    private void preventAmbrosialArborBreak(int damage, ServerLevel level, @Nullable LivingEntity entity, Consumer<Item> onBreak, CallbackInfo callback) {
        if (damage <= 0) return;
        if (AmbrosialArborAttachment.preventBreak(level, entity, (ItemStack) (Object) this)) callback.cancel();
    }
}
