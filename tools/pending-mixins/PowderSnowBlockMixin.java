package net.per.primogemcraft.mixin;

import net.per.primogemcraft.compat.attachment.Attachments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.PowderSnowBlock;
import net.per.primogemcraft.registry.PGCAttachments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowBlockMixin {
    @Inject(method = "canEntityWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void canEntityWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof PlayerEntity player && Attachments.get(player, PGCAttachments.POWDER_SNOW_WALK)) cir.setReturnValue(true);
    }
}
