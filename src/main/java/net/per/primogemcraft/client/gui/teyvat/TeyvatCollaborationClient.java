package net.per.primogemcraft.client.gui.teyvat;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.per.primogemcraft.collab.teyvatdelight.StellarShopNetwork;
import net.per.primogemcraft.collab.teyvatdelight.TeyvatDelightIntegration;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class TeyvatCollaborationClient {
    private TeyvatCollaborationClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        if (TeyvatDelightIntegration.supportsShop())
            event.enqueueWork(() -> {
                StellarShopNetwork.setReceiver(KatheryneCollaboration::receive);
                NeoForge.EVENT_BUS.addListener(KatheryneCollaborationScreen::addEntry);
            });
    }
}
