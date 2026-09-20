package net.per.primogemcraft.collab.teyvatdelight;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public final class TeyvatDelightIntegration {
    private TeyvatDelightIntegration() {
    }

    public static void register(IEventBus modBus) {
        if (ModList.get().isLoaded("teyvatdelight")) TeyvatDelightBridge.register(modBus);
    }
}
