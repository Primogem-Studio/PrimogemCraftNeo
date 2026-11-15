package net.per.primogemcraft.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.per.primogemcraft.command.debug.Curio;
import net.per.primogemcraft.command.debug.Enchant;
import net.per.primogemcraft.command.debug.Event;
import net.per.primogemcraft.command.debug.Weapon;
import net.per.primogemcraft.command.debug.Wish;
import net.per.primogemcraft.command.debug.Zipline;

public class PGCCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Wish.register(dispatcher);
            Curio.register(dispatcher);
            Enchant.register(dispatcher);
            Event.register(dispatcher);
            Weapon.register(dispatcher, registryAccess);
            Zipline.register(dispatcher);
        });
    }
}