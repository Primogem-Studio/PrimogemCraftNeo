package net.per.primogemcraft.collab.tacz;

import net.fabricmc.loader.api.FabricLoader;

/**
 * The TACZ half of the firearm linkage. TACZ is an optional dependency: nothing here reaches TACZ types,
 * and only while the mod is installed does {@link TaczBridge} — the one class that does — get registered.
 */
public final class TaczIntegration {
    private static final String TACZ_MOD_ID = "tacz";

    private static Boolean loaded;

    private TaczIntegration() {
    }

    public static void register() {
        if (loaded()) TaczBridge.register();
    }

    private static boolean loaded() {
        if (loaded == null) loaded = FabricLoader.getInstance().isModLoaded(TACZ_MOD_ID);
        return loaded;
    }
}
