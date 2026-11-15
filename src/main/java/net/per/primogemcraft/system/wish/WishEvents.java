package net.per.primogemcraft.system.wish;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.WishMaterialsPayload;
import net.per.primogemcraft.network.WishDropsPayload;

public class WishEvents {
    public static void register() {
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            var payload = new WishMaterialsPayload(WishValueMaterials.displayValues());
            var drops = new WishDropsPayload(WishDrops.displayItems());
            PayloadRegistrar.sendToPlayer(player, payload, drops);
        });
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new WishValueMaterials());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new WishDrops());
    }
}