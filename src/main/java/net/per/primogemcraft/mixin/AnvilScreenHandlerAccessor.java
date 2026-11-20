package net.per.primogemcraft.mixin;

import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 暴露铁砧的等级消耗字段。
 *
 * <p>1.20.1 的 {@code AnvilScreenHandler.levelCost} 是私有 {@link Property}，
 * 只有 getter 没有 setter；十连祈愿核心合成需要把消耗置零，故用访问器读写。
 */
@Mixin(AnvilScreenHandler.class)
public interface AnvilScreenHandlerAccessor {
    @Accessor("levelCost")
    Property primogemcraft$getLevelCostProperty();
}