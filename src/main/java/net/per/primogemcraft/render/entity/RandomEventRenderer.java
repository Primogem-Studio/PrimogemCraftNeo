package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.per.primogemcraft.entity.misc.RandomEventEntity;
import net.per.primogemcraft.system.event.EventRegistry;

public class RandomEventRenderer extends EntityRenderer<RandomEventEntity> {
    private static final float GROUND_TRANSLATION_Y = 17.02F / 16.0F;
    private static final float GROUND_SCALE_X = 2.38F;
    private static final float GROUND_SCALE_Y = 2.98F;
    private static final float GROUND_SCALE_Z = 2.38F;
    private static final float BOB_AMPLITUDE = 0.1F;
    private static final float BOB_PERIOD = 10.0F;
    private static final float SPIN_PERIOD = 20.0F;
    private static final float HALF_SIZE = 0.5F;
    private static final float HALF_THICKNESS = 1.0F / 32.0F;
    private static final float EDGE_UV = 1.0F / 16.0F;

    public RandomEventRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(RandomEventEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        var age = entity.tickCount + partialTick;
        poseStack.pushPose();
        poseStack.translate(0.0F, Mth.sin(age / BOB_PERIOD + entity.bobOffs) * BOB_AMPLITUDE + BOB_AMPLITUDE + 0.25F * GROUND_SCALE_Y, 0.0F);
        poseStack.mulPose(Axis.YP.rotation(age / SPIN_PERIOD + entity.bobOffs));
        poseStack.translate(0.0F, GROUND_TRANSLATION_Y, 0.0F);
        poseStack.scale(GROUND_SCALE_X, GROUND_SCALE_Y, GROUND_SCALE_Z);
        var pose = poseStack.last();
        var consumer = bufferSource.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        panel(consumer, pose, HALF_THICKNESS, 1.0F, -HALF_SIZE);
        panel(consumer, pose, -HALF_THICKNESS, -1.0F, HALF_SIZE);
        verticalRim(consumer, pose, HALF_SIZE, 1.0F, HALF_THICKNESS, 1.0F - EDGE_UV, 1.0F);
        verticalRim(consumer, pose, -HALF_SIZE, -1.0F, -HALF_THICKNESS, 0.0F, EDGE_UV);
        horizontalRim(consumer, pose, HALF_SIZE, 1.0F, -HALF_THICKNESS, 0.0F, EDGE_UV);
        horizontalRim(consumer, pose, -HALF_SIZE, -1.0F, HALF_THICKNESS, 1.0F - EDGE_UV, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RandomEventEntity entity) {
        var group = entity.group();
        return group == null ? EventRegistry.DEFAULT_TEXTURE : group.textureOrDefault();
    }

    private static void panel(VertexConsumer consumer, PoseStack.Pose pose, float z, float normalZ, float uZeroX) {
        var uOneX = -uZeroX;
        vertex(consumer, pose, uZeroX, HALF_SIZE, z, 0.0F, 0.0F, 0.0F, 0.0F, normalZ);
        vertex(consumer, pose, uZeroX, -HALF_SIZE, z, 0.0F, 1.0F, 0.0F, 0.0F, normalZ);
        vertex(consumer, pose, uOneX, -HALF_SIZE, z, 1.0F, 1.0F, 0.0F, 0.0F, normalZ);
        vertex(consumer, pose, uOneX, HALF_SIZE, z, 1.0F, 0.0F, 0.0F, 0.0F, normalZ);
    }

    private static void verticalRim(VertexConsumer consumer, PoseStack.Pose pose, float x, float normalX, float vZeroZ, float vZeroU, float vOneU) {
        var vOneZ = -vZeroZ;
        vertex(consumer, pose, x, HALF_SIZE, vZeroZ, vZeroU, 0.0F, normalX, 0.0F, 0.0F);
        vertex(consumer, pose, x, -HALF_SIZE, vZeroZ, vZeroU, 1.0F, normalX, 0.0F, 0.0F);
        vertex(consumer, pose, x, -HALF_SIZE, vOneZ, vOneU, 1.0F, normalX, 0.0F, 0.0F);
        vertex(consumer, pose, x, HALF_SIZE, vOneZ, vOneU, 0.0F, normalX, 0.0F, 0.0F);
    }

    private static void horizontalRim(VertexConsumer consumer, PoseStack.Pose pose, float y, float normalY, float uZeroZ, float vZero, float vOne) {
        var uOneZ = -uZeroZ;
        vertex(consumer, pose, -HALF_SIZE, y, uZeroZ, 0.0F, vZero, 0.0F, normalY, 0.0F);
        vertex(consumer, pose, -HALF_SIZE, y, uOneZ, 0.0F, vOne, 0.0F, normalY, 0.0F);
        vertex(consumer, pose, HALF_SIZE, y, uOneZ, 1.0F, vOne, 0.0F, normalY, 0.0F);
        vertex(consumer, pose, HALF_SIZE, y, uZeroZ, 1.0F, vZero, 0.0F, normalY, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, normalX, normalY, normalZ);
    }
}
