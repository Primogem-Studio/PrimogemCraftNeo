package net.per.primogemcraft.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.Map;

public final class WishMaterialValues {
    private static Map<Identifier, Integer> values = Map.of();

    public static Map<Identifier, Integer> get() {
        return values;
    }

    public static void update(Map<Identifier, Integer> materials) {
        values = Map.copyOf(materials);
    }

    public static void register() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> values = Map.of());
    }
}
