package net.per.primogemcraft.system.wish;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.network.WishMaterialsPayload;
import net.per.primogemcraft.network.WishDropsPayload;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public class WishEvents {
    @SubscribeEvent
    public static void syncMaterials(OnDatapackSyncEvent event) {
        var payload = new WishMaterialsPayload(WishValueMaterials.displayValues());
        var drops = new WishDropsPayload(WishDrops.displayItems());
        if (event.getPlayer() != null) PacketDistributor.sendToPlayer(event.getPlayer(), payload, drops);
        else PacketDistributor.sendToAllPlayers(payload, drops);
    }

    @SubscribeEvent
    public static void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new WishValueMaterials());
        event.addListener(new WishDrops());
    }
}
