package net.per.primogemcraft.collab.tacz;

import net.neoforged.fml.ModList;

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
        if (loaded == null) loaded = ModList.get().isLoaded(TACZ_MOD_ID);
        return loaded;
    }
}
