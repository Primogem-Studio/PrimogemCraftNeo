package net.per.primogemcraft.collab.teyvatdelight;

import net.fabricmc.loader.api.FabricLoader;

public final class TeyvatDelightIntegration {
    private TeyvatDelightIntegration() {
    }

    public static void register() {
        if (FabricLoader.getInstance().isModLoaded("teyvatdelight")) TeyvatDelightBridge.register();
    }
}
