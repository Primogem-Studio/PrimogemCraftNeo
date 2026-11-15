package net.per.primogemcraft.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.render.entity.AbundanceBlightZombieRenderer;
import net.per.primogemcraft.render.entity.DendroCoreRenderer;
import net.per.primogemcraft.render.entity.HertaOtherworldBranchTowerRenderer;
import net.per.primogemcraft.render.entity.LivingItemRenderer;
import net.per.primogemcraft.render.entity.RandomEventRenderer;
import net.per.primogemcraft.render.entity.WishEntityRenderer;
import net.per.primogemcraft.render.entity.XiaoLanternRenderer;
import net.per.primogemcraft.render.entity.ZiplineAnchorRenderer;
import net.per.primogemcraft.render.entity.ZiplineCarrierRenderer;
import net.per.primogemcraft.render.entity.model.DendroCoreModel;
import net.per.primogemcraft.render.entity.model.HertaOtherworldBranchTowerModel;
import net.per.primogemcraft.render.entity.model.WishEntityModel;
import net.per.primogemcraft.render.entity.model.XiaoLanternModel;

/**
 * 客户端渲染注册。
 *
 * <p>渲染器与模型层必须在客户端入口注册，否则 {@code EntityRenderDispatcher.getRenderer}
 * 返回 null，玩家一看到实体就会在渲染线程抛 NullPointerException。
 */
public final class PGCClientRenderers {
    private PGCClientRenderers() {
    }

    public static void register() {
        // 自定义模型层（先注册层，渲染器构造时才能取到 ModelPart）
        EntityModelLayerRegistry.registerModelLayer(WishEntityModel.LAYER_LOCATION, WishEntityModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DendroCoreModel.LAYER, DendroCoreModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HertaOtherworldBranchTowerModel.LAYER, HertaOtherworldBranchTowerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(XiaoLanternModel.LAYER, XiaoLanternModel::createBodyLayer);

        // 实体渲染器
        EntityRendererRegistry.register(PGCEntities.WISH_ENTITY.get(), WishEntityRenderer::new);
        EntityRendererRegistry.register(PGCEntities.DENDRO_CORE.get(), DendroCoreRenderer::new);
        EntityRendererRegistry.register(PGCEntities.HERTA_OTHERWORLD_BRANCH_TOWER.get(), HertaOtherworldBranchTowerRenderer::new);
        EntityRendererRegistry.register(PGCEntities.XIAO_LANTERN.get(), XiaoLanternRenderer::new);
        EntityRendererRegistry.register(PGCEntities.ABUNDANCE_BLIGHT_ZOMBIE.get(), AbundanceBlightZombieRenderer::new);
        EntityRendererRegistry.register(PGCEntities.RANDOM_EVENT.get(), RandomEventRenderer::new);
        EntityRendererRegistry.register(PGCEntities.LIVING_ITEM.get(), LivingItemRenderer::new);
        EntityRendererRegistry.register(PGCEntities.ZIPLINE_ANCHOR.get(), ZiplineAnchorRenderer::new);
        EntityRendererRegistry.register(PGCEntities.ZIPLINE_CARRIER.get(), ZiplineCarrierRenderer::new);

        // 下落摩拉堆沿用原版下落方块渲染
        EntityRendererRegistry.register(PGCEntities.FALLING_MORA_PILE.get(), FallingBlockEntityRenderer::new);
    }
}