package net.per.primogemcraft.system.curio.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioItem;

import java.util.List;
import java.util.Set;

/**
 * 饰品兼容集成（Fabric）。
 * <p>原模组用 Curios；Fabric 侧改用 Accessories。检测 Accessories 是否加载。
 */
public final class CuriosIntegration {
    private static final String ACCESSORIES_MOD_ID = "accessories";

    private static Boolean loaded;

    private CuriosIntegration() {
    }

    public static void onCommonSetup() {
        if (loaded()) CuriosBridge.register();
    }

    public static boolean loaded() {
        if (loaded == null) loaded = FabricLoader.getInstance().isModLoaded(ACCESSORIES_MOD_ID);
        return loaded;
    }

    public static List<ItemStack> equipped(PlayerEntity player) {
        return loaded() ? CuriosBridge.equipped(player) : List.of();
    }

    public static int consume(ServerPlayerEntity player, Set<Item> pending) {
        return loaded() ? CuriosBridge.consume(player, pending) : 0;
    }

    public static boolean equip(ServerPlayerEntity player, ItemStack stack) {
        return loaded() && CuriosBridge.equip(player, stack);
    }

    public static void tick(ServerPlayerEntity player) {
        if (loaded()) CuriosBridge.tick(player);
    }

    public static void collect(ServerPlayerEntity player, List<CurioContext> output) {
        if (!loaded()) return;
        for (var stack : CuriosBridge.equipped(player))
            if (stack.getItem() instanceof CurioItem curio) output.add(CurioContext.of(player, stack, curio.form()));
    }
}
