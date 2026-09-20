package net.per.primogemcraft.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

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
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        values = Map.of();
    }
}
