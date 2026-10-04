package net.per.primogemcraft.collab.teyvatdelight;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public final class TeyvatDelightIntegration {
    private TeyvatDelightIntegration() {
    }

    public static void register(IEventBus modBus) {
        if (ModList.get().isLoaded("teyvatdelight")) TeyvatDelightBridge.register(modBus);
    }

    /** Reports whether the installed optional mod supplies the Katheryne shop API. */
    public static boolean supportsShop() {
        return ModList.get().getModContainerById("teyvatdelight")
                .map(container -> container.getModInfo().getVersion().compareTo(new DefaultArtifactVersion("1.0.3")) >= 0)
                .orElse(false);
    }
}
