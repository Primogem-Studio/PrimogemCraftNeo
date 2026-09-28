package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.per.primogemcraft.entity.misc.WishArrowEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class WishArrowRenderer extends ArrowRenderer<WishArrowEntity> {
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/projectiles/wish_arrow_beams.png");

    public WishArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WishArrowEntity arrow, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, arrow.yRotO, arrow.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, arrow.xRotO, arrow.getXRot())));
        var pose = poseStack.last();
        var consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(BEAM_TEXTURE, false));
        var row = arrow.isAnemo() ? 1 : arrow.isEmpowered() ? 6 : 0;
        var v0 = row / 8.0F;
        var v1 = (row + 1) / 8.0F;
        beam(consumer, pose, v0, v1);
        beam(bufferSource.getBuffer(RenderType.eyes(BEAM_TEXTURE)), pose, v0, v1);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(WishArrowEntity arrow) {
        return arrow.texture();
    }

    private static void beam(VertexConsumer consumer, PoseStack.Pose pose, float v0, float v1) {
        vertex(consumer, pose, -1.45F, -0.12F, 0.0F, 0.0F, v1);
        vertex(consumer, pose, -1.45F, 0.12F, 0.0F, 0.0F, v0);
        vertex(consumer, pose, -0.08F, 0.12F, 0.0F, 1.0F, v0);
        vertex(consumer, pose, -0.08F, -0.12F, 0.0F, 1.0F, v1);
        vertex(consumer, pose, -1.45F, 0.0F, -0.12F, 0.0F, v1);
        vertex(consumer, pose, -1.45F, 0.0F, 0.12F, 0.0F, v0);
        vertex(consumer, pose, -0.08F, 0.0F, 0.12F, 1.0F, v0);
        vertex(consumer, pose, -0.08F, 0.0F, -0.12F, 1.0F, v1);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
