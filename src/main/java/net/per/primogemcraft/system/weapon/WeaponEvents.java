package net.per.primogemcraft.system.weapon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class WeaponEvents {
    private WeaponEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerManager().getPlayerList()) {
                WeaponAttributes.refreshLevels(player);
            }
        });
    }
}