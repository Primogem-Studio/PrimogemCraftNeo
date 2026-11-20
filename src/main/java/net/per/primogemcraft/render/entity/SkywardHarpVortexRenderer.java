package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.entity.misc.SkywardHarpVortexEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class SkywardHarpVortexRenderer extends EntityRenderer<SkywardHarpVortexEntity> {
    private static final Identifier TEXTURE = new Identifier(MOD_ID, "textures/entity/skyward_harp_vortex.png");
    private static final RenderLayer RENDER_TYPE = RenderLayer.getEntityTranslucent(TEXTURE);

    public SkywardHarpVortexRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(SkywardHarpVortexEntity entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider buffer, int packedLight) {
        poseStack.push();
        poseStack.multiply(entityRenderDispatcher.cameraOrientation());
        if (entity.isWindEye()) poseStack.scale(0.4F, 0.4F, 0.4F);
        var alpha = entity.isWindEye() ? 191 : 170;
        var frame = (int) (entity.getWorld().getTime() / 2L % 5L);
        var v0 = frame / 5.0F;
        var v1 = (frame + 1) / 5.0F;
        var pose = poseStack.peek();
        var consumer = buffer.getBuffer(RENDER_TYPE);
        vertex(consumer, pose, -2.5F, -2.5F, 0.0F, v1, alpha);
        vertex(consumer, pose, 2.5F, -2.5F, 1.0F, v1, alpha);
        vertex(consumer, pose, 2.5F, 2.5F, 1.0F, v0, alpha);
        vertex(consumer, pose, -2.5F, 2.5F, 0.0F, v0, alpha);
        poseStack.pop();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public Identifier getTexture(SkywardHarpVortexEntity entity) {
        return TEXTURE;
    }

    private static void vertex(VertexConsumer consumer, MatrixStack.Entry pose, float x, float y, float u, float v, int alpha) {
        consumer.vertex(pose.getMatrices(), x, y, 0.0F).color(255, 255, 255, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.DEFAULT_UV).uv2(LightmapTextureManager.FULL_BRIGHT)
                .getNormalMatrix(pose.getNormalMatrix(), 0.0F, 0.0F, 1.0F).endVertex();
    }
}
