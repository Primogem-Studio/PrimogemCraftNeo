package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.render.entity.model.XiaoLanternModel;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class XiaoLanternRenderer extends EntityRenderer<XiaoLanternEntity> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/xiao_lantern.png");
    private static final float SHADOW_RADIUS = 0.5F;
    private static final int GLOW_COLOR = 0xFFFFFFFF;

    private final XiaoLanternModel model;

    public XiaoLanternRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new XiaoLanternModel(context.getPart(XiaoLanternModel.LAYER));
        shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public void render(XiaoLanternEntity entity, float entityYaw, float partialTick, net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        model.render(poseStack, bufferSource.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        model.render(poseStack, bufferSource.getBuffer(RenderLayer.getEyes(TEXTURE)), packedLight, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public Identifier getTexture(XiaoLanternEntity entity) {
        return TEXTURE;
    }
}
