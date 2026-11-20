package net.per.primogemcraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.render.entity.SkywardHarpVortexRenderer;
import net.per.primogemcraft.render.entity.WishArrowRenderer;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 弓系统客户端注册。
 *
 * <p>Forge 用 {@code EntityRenderersEvent.RegisterRenderers} 与
 * {@code RegisterClientReloadListenersEvent}；Fabric 侧改为在客户端入口显式调用本类。
 */
@Environment(EnvType.CLIENT)
public final class WishBowClient {
    private WishBowClient() {
    }

    public static void register() {
        EntityRendererRegistry.register(PGCEntities.WISH_ARROW.get(), WishArrowRenderer::new);
        EntityRendererRegistry.register(PGCEntities.SKYWARD_HARP_VORTEX.get(), SkywardHarpVortexRenderer::new);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return new Identifier(MOD_ID, "wish_bow_meshes");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        // Forge 的 IClientItemExtensions 手持变换在 Fabric 无对应机制
                    }
                });
    }
}