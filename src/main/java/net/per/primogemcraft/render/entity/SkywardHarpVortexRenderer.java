package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.entity.misc.SkywardHarpVortexEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class SkywardHarpVortexRenderer extends EntityRenderer<SkywardHarpVortexEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/skyward_harp_vortex.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucentEmissive(TEXTURE, false);

    public SkywardHarpVortexRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(SkywardHarpVortexEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        if (entity.isWindEye()) poseStack.scale(0.4F, 0.4F, 0.4F);
        var alpha = entity.isWindEye() ? 191 : 170;
        var frame = (int) (entity.level().getGameTime() / 2L % 5L);
        var v0 = frame / 5.0F;
        var v1 = (frame + 1) / 5.0F;
        var pose = poseStack.last();
        var consumer = buffer.getBuffer(RENDER_TYPE);
        vertex(consumer, pose, -2.5F, -2.5F, 0.0F, v1, alpha);
        vertex(consumer, pose, 2.5F, -2.5F, 1.0F, v1, alpha);
        vertex(consumer, pose, 2.5F, 2.5F, 1.0F, v0, alpha);
        vertex(consumer, pose, -2.5F, 2.5F, 0.0F, v0, alpha);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SkywardHarpVortexEntity entity) {
        return TEXTURE;
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v, int alpha) {
        consumer.addVertex(pose, x, y, 0.0F).setColor(255, 255, 255, alpha).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
