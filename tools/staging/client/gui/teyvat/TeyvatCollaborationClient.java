package net.per.primogemcraft.client.gui.teyvat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.per.primogemcraft.collab.teyvatdelight.StellarShopNetwork;
import net.per.primogemcraft.collab.teyvatdelight.TeyvatDelightIntegration;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class TeyvatCollaborationClient {
    private TeyvatCollaborationClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        if (TeyvatDelightIntegration.supportsShop())
            event.enqueueWork(() -> {
                StellarShopNetwork.setReceiver(KatheryneCollaboration::receive);
                MinecraftForge.EVENT_BUS.addListener(KatheryneCollaborationScreen::addEntry);
            });
    }
}
