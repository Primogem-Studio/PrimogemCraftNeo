package net.per.primogemcraft.mixin;

import net.minecraft.screen.AnvilScreenHandler;
import net.per.primogemcraft.system.wish.WishCoreAnvil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 替代 Forge 的 {@code AnvilUpdateEvent}：原版结果算完后，追加十连祈愿核心的合成判定。
 */
@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void primogemcraft$wishCoreAnvil(CallbackInfo info) {
        WishCoreAnvil.update((AnvilScreenHandler) (Object) this);
    }
}