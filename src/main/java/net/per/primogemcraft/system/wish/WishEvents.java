package net.per.primogemcraft.system.wish;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.WishMaterialsPayload;
import net.per.primogemcraft.network.WishDropsPayload;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class WishEvents {
    @SubscribeEvent
    public static void syncMaterials(OnDatapackSyncEvent event) {
        var payload = new WishMaterialsPayload(WishValueMaterials.displayValues());
        var drops = new WishDropsPayload(WishDrops.displayItems());
        if (event.getPlayer() != null) PayloadRegistrar.sendToPlayer(event.getPlayer(), payload, drops);
        else PayloadRegistrar.sendToAllPlayers(payload, drops);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new WishValueMaterials());
        event.addListener(new WishDrops());
    }
}
