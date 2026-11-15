package net.per.primogemcraft.system.curio.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioItem;

import java.util.List;
import java.util.Set;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CuriosIntegration {
    private static final String CURIO_MOD_ID = "curios";

    private static Boolean loaded;

    private CuriosIntegration() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (loaded()) CuriosBridge.register();
    }

    public static boolean loaded() {
        if (loaded == null) loaded = ModList.get().isLoaded(CURIO_MOD_ID);
        return loaded;
    }

    public static List<ItemStack> equipped(Player player) {
        return loaded() ? CuriosBridge.equipped(player) : List.of();
    }

    public static int consume(ServerPlayer player, Set<Item> pending) {
        return loaded() ? CuriosBridge.consume(player, pending) : 0;
    }

    public static boolean equip(ServerPlayer player, ItemStack stack) {
        return loaded() && CuriosBridge.equip(player, stack);
    }

    public static void tick(ServerPlayer player) {
        if (loaded()) CuriosBridge.tick(player);
    }

    public static void collect(ServerPlayer player, List<CurioContext> output) {
        if (!loaded()) return;
        for (var stack : CuriosBridge.equipped(player))
            if (stack.getItem() instanceof CurioItem curio) output.add(CurioContext.of(player, stack, curio.form()));
    }
}
