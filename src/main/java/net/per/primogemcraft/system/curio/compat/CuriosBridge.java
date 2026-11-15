package net.per.primogemcraft.system.curio.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.Curios;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 饰品桥接（Fabric）。
 * <p>直接使用模组自身的 Curios 系统，不依赖外部 Curios/Accessories API。
 */
public final class CuriosBridge {
    private static final String TIMER_PREFIX = "curio_equipped_";

    private CuriosBridge() {
    }

    public static void register() {
        // 模组自身 Curios 系统已在 PGCItems 注册时完成初始化
    }

    public static List<ItemStack> equipped(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return List.of();
        var stacks = new ArrayList<ItemStack>();
        for (var context : Curios.held(serverPlayer)) {
            stacks.add(context.stack());
        }
        return stacks;
    }

    public static boolean equip(ServerPlayerEntity player, ItemStack stack) {
        return Curios.equip(player, stack);
    }

    public static int consume(ServerPlayerEntity player, Set<Item> pending) {
        var consumed = 0;
        for (var context : Curios.held(player)) {
            var stack = context.stack();
            if (stack.isEmpty() || !pending.contains(stack.getItem())) continue;
            stack.decrement(1);
            pending.remove(stack.getItem());
            consumed++;
            if (pending.isEmpty()) break;
        }
        return consumed;
    }

    public static void tick(ServerPlayerEntity player) {
        var contexts = Curios.held(player);
        for (var index = 0; index < contexts.size(); index++) {
            var context = contexts.get(index);
            if (!(context.stack().getItem() instanceof CurioItem curio)) continue;
            if (context.ready(TIMER_PREFIX + index, curio.presenceInterval())) curio.presence(context);
        }
    }
}
