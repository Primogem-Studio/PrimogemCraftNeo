package net.per.primogemcraft.collab.elixir;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

/**
 * The Elixir Dan half of the alchemy linkage. Elixir Dan is an optional dependency: nothing here reaches
 * Elixir Dan types, and only while the mod is installed does {@link ElixirBridge} — the one class that does —
 * get registered.
 */
public final class ElixirIntegration {
    private static final String ELIXIR_MOD_ID = "elixir";

    private static Boolean loaded;

    private ElixirIntegration() {
    }

    public static void register(IEventBus modBus) {
        if (loaded()) ElixirBridge.register(modBus);
    }

    private static boolean loaded() {
        if (loaded == null) loaded = ModList.get().isLoaded(ELIXIR_MOD_ID);
        return loaded;
    }
}
