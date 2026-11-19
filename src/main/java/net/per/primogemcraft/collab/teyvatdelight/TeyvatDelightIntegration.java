package net.per.primogemcraft.collab.teyvatdelight;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

public final class TeyvatDelightIntegration {
    private TeyvatDelightIntegration() {
    }

    public static void register(IEventBus modBus) {
        if (ModList.get().isLoaded("teyvatdelight")) TeyvatDelightBridge.register(modBus);
    }

    public static boolean supportsShop() {
        return ModList.get().isLoaded("teyvatdelight");
    }
}
