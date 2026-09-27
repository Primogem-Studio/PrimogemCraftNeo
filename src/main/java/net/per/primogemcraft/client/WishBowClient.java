package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.per.primogemcraft.network.WishBowShootPayload;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.render.entity.WishArrowRenderer;
import net.per.primogemcraft.system.weapon.WishWeaponBowItem;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class WishBowClient {
    private static WishBowRenderer renderer;
    private static boolean attackHeld;
    private static int lastRequestTick = Integer.MIN_VALUE;

    @SubscribeEvent
    public static void input(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) return;
        if (event.isCanceled()) {
            attackHeld = false;
            return;
        }
        var minecraft = Minecraft.getInstance();
        if (!canShoot(minecraft)) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        attackHeld = true;
        requestShot(minecraft);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Pre event) {
        var minecraft = Minecraft.getInstance();
        if (!canShoot(minecraft) || !minecraft.options.keyAttack.isDown()) {
            attackHeld = false;
            lastRequestTick = Integer.MIN_VALUE;
            return;
        }
        if (attackHeld) requestShot(minecraft);
    }

    private static boolean canShoot(Minecraft minecraft) {
        var player = minecraft.player;
        return minecraft.screen == null && minecraft.isWindowActive() && !minecraft.isPaused()
                && player != null && player.isAlive() && !player.isSpectator() && !player.isUsingItem()
                && player.getMainHandItem().getItem() instanceof WishWeaponBowItem;
    }

    private static void requestShot(Minecraft minecraft) {
        var tick = minecraft.player.tickCount;
        if (tick == lastRequestTick) return;
        lastRequestTick = tick;
        PacketDistributor.sendToServer(new WishBowShootPayload());
    }

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        var extension = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new WishBowRenderer();
                return renderer;
            }
        };
        for (var entry : PGCItems.REGISTRY.getEntries())
            if (entry.get() instanceof WishWeaponBowItem) event.registerItem(extension, entry.get());
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resources -> {
            if (renderer != null) renderer.onResourceManagerReload(resources);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PGCEntities.WISH_ARROW.get(), WishArrowRenderer::new);
    }
}
