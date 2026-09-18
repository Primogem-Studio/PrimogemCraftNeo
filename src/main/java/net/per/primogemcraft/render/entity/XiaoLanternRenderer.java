package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.render.entity.model.XiaoLanternModel;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class XiaoLanternRenderer extends EntityRenderer<XiaoLanternEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/xiao_lantern.png");
    private static final float SHADOW_RADIUS = 0.5F;
    private static final int GLOW_COLOR = 0xFFFFFFFF;

    private final XiaoLanternModel model;

    public XiaoLanternRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new XiaoLanternModel(context.bakeLayer(XiaoLanternModel.LAYER));
        shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public void render(XiaoLanternEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, GLOW_COLOR);
        model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.eyes(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY, GLOW_COLOR);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(XiaoLanternEntity entity) {
        return TEXTURE;
    }
}
