package net.per.primogemcraft.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.per.primogemcraft.compat.event.EventDispatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 替代 Forge 的 {@code LivingDropsEvent}。
 *
 * <p>1.20.1 的 {@code LivingEntity.dropLoot} 把战利品直接交给 Consumer 生成掉落物，
 * 没有可修改的掉落列表。这里把该 Consumer 换成收集器，待原逻辑跑完后统一分发给监听者，
 * 再把追加的物品一并生成，等价于 Forge 事件的 {@code getDrops().add(...)}。
 */
@Mixin(LivingEntity.class)
public class LivingEntityDropsMixin {

    @Redirect(method = "dropLoot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContextParameterSet;JLjava/util/function/Consumer;)V"))
    private void primogemcraft$collectDrops(LootTable table, LootContextParameterSet params, long seed,
                                            Consumer<ItemStack> original) {
        var self = (LivingEntity) (Object) this;
        var collected = new ArrayList<ItemStack>();
        table.generateLoot(params, seed, collected::add);
        for (var stack : collected) original.accept(stack);

        var extra = new ArrayList<ItemEntity>();
        EventDispatch.fireLivingDrops(self, self.getRecentDamageSource(), extra);
        for (var drop : extra) {
            if (drop == null || drop.getStack().isEmpty()) continue;
            original.accept(drop.getStack());
        }
    }
}