package net.per.primogemcraft.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.per.primogemcraft.command.debug.Curio;
import net.per.primogemcraft.command.debug.Enchant;
import net.per.primogemcraft.command.debug.Event;
import net.per.primogemcraft.command.debug.Weapon;
import net.per.primogemcraft.command.debug.Wish;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class PGCCommands {
    @SubscribeEvent
    private static void on(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        Wish.register(dispatcher);
        Curio.register(dispatcher);
        Enchant.register(dispatcher);
        Event.register(dispatcher);
        Weapon.register(dispatcher, event.getBuildContext());
    }
}
