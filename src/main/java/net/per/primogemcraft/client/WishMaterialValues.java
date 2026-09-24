package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.Map;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WishMaterialValues {
    private static Map<ResourceLocation, Integer> values = Map.of();

    public static Map<ResourceLocation, Integer> get() {
        return values;
    }

    public static void update(Map<ResourceLocation, Integer> materials) {
        values = Map.copyOf(materials);
    }

    @SubscribeEvent
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (minecraft.screen != null || player == null || !player.getMainHandItem().is(PGCItems.WISH_CORE.get())) return;
        var source = player.getOffhandItem();
        var value = source.isEmpty() ? 0 : values.getOrDefault(BuiltInRegistries.ITEM.getKey(source.getItem()), 1);
        player.displayClientMessage(WishReports.offhandValue(value), false);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        values = Map.of();
    }
}
