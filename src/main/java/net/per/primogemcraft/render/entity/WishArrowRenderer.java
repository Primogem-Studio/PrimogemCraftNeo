package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.entity.misc.WishArrowEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class WishArrowRenderer extends ProjectileEntityRenderer<WishArrowEntity> {
    private static final Identifier BEAM_TEXTURE = new Identifier(MOD_ID, "textures/entity/projectiles/wish_arrow_beams.png");

    public WishArrowRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(WishArrowEntity arrow, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        super.render(arrow, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.push();
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(partialTick, arrow.prevYaw, arrow.getYaw()) - 90.0F));
        poseStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(partialTick, arrow.prevPitch, arrow.getPitch())));
        var pose = poseStack.peek();
        var row = arrow.isAnemo() ? 1 : arrow.isEmpowered() || arrow.isFireArrow() || arrow.isOnFire() ? 6 : 0;
        var v0 = row / 8.0F;
        var v1 = (row + 1) / 8.0F;
        beam(bufferSource.getBuffer(RenderLayer.getEntityTranslucent(BEAM_TEXTURE)), pose, v0, v1);
        beam(bufferSource.getBuffer(RenderLayer.getEyes(BEAM_TEXTURE)), pose, v0, v1);
        poseStack.pop();
    }

    @Override
    public Identifier getTexture(WishArrowEntity arrow) {
        return arrow.texture();
    }

    private static void beam(VertexConsumer consumer, MatrixStack.Entry pose, float v0, float v1) {
        vertex(consumer, pose, -1.45F, -0.12F, 0.0F, 0.0F, v1);
        vertex(consumer, pose, -1.45F, 0.12F, 0.0F, 0.0F, v0);
        vertex(consumer, pose, -0.08F, 0.12F, 0.0F, 1.0F, v0);
        vertex(consumer, pose, -0.08F, -0.12F, 0.0F, 1.0F, v1);
        vertex(consumer, pose, -1.45F, 0.0F, -0.12F, 0.0F, v1);
        vertex(consumer, pose, -1.45F, 0.0F, 0.12F, 0.0F, v0);
        vertex(consumer, pose, -0.08F, 0.0F, 0.12F, 1.0F, v0);
        vertex(consumer, pose, -0.08F, 0.0F, -0.12F, 1.0F, v1);
    }

    private static void vertex(VertexConsumer consumer, MatrixStack.Entry pose, float x, float y, float z, float u, float v) {
        consumer.vertex(pose.getPositionMatrix(), x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .normal(pose.getNormalMatrix(), 0.0F, 0.0F, 1.0F)
                .next();
    }
}
