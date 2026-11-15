package net.per.primogemcraft.command;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.per.primogemcraft.command.debug.Curio;
import net.per.primogemcraft.command.debug.Enchant;
import net.per.primogemcraft.command.debug.Event;
import net.per.primogemcraft.command.debug.Weapon;
import net.per.primogemcraft.command.debug.Wish;
import net.per.primogemcraft.command.debug.Zipline;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class PGCCommands {
    @SubscribeEvent
    public static void on(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        Wish.register(dispatcher);
        Curio.register(dispatcher);
        Enchant.register(dispatcher);
        Event.register(dispatcher);
        Weapon.register(dispatcher, event.getBuildContext());
        Zipline.register(dispatcher);
    }
}
