package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.entity.misc.DendroCoreEntity;
import net.per.primogemcraft.render.entity.model.DendroCoreModel;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DendroCoreRenderer extends EntityRenderer<DendroCoreEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/dendro_core.png");
    private static final float SHADOW_RADIUS = 0.2F;
    private static final float MODEL_MIRROR = -1.0F;
    private static final float MODEL_GROUND_OFFSET = -1.501F;

    private final DendroCoreModel model;

    public DendroCoreRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new DendroCoreModel(context.bakeLayer(DendroCoreModel.LAYER));
        shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public void render(DendroCoreEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(MODEL_MIRROR, MODEL_MIRROR, 1.0F);
        poseStack.translate(0.0F, MODEL_GROUND_OFFSET, 0.0F);
        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DendroCoreEntity entity) {
        return TEXTURE;
    }
}
