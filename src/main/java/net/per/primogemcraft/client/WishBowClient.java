package net.per.primogemcraft.client;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.render.entity.SkywardHarpVortexRenderer;
import net.per.primogemcraft.render.entity.WishArrowRenderer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WishBowClient {
    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) WishBowClientExtensions::clearMeshes);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PGCEntities.WISH_ARROW.get(), WishArrowRenderer::new);
        event.registerEntityRenderer(PGCEntities.SKYWARD_HARP_VORTEX.get(), SkywardHarpVortexRenderer::new);
    }
}
