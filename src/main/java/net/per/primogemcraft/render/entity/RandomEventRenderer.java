package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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

    public RandomEventRenderer(EntityRendererFactory.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(RandomEventEntity entity, float entityYaw, float partialTick, net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        var age = entity.age + partialTick;
        poseStack.push();
        poseStack.translate(0.0F, MathHelper.sin(age / BOB_PERIOD + entity.bobOffs) * BOB_AMPLITUDE + BOB_AMPLITUDE + 0.25F * GROUND_SCALE_Y, 0.0F);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotation(age / SPIN_PERIOD + entity.bobOffs));
        poseStack.translate(0.0F, GROUND_TRANSLATION_Y, 0.0F);
        poseStack.scale(GROUND_SCALE_X, GROUND_SCALE_Y, GROUND_SCALE_Z);
        var pose = poseStack.peek();
        var consumer = bufferSource.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
        panel(consumer, pose, HALF_THICKNESS, 1.0F, -HALF_SIZE);
        panel(consumer, pose, -HALF_THICKNESS, -1.0F, HALF_SIZE);
        verticalRim(consumer, pose, HALF_SIZE, 1.0F, HALF_THICKNESS, 1.0F - EDGE_UV, 1.0F);
        verticalRim(consumer, pose, -HALF_SIZE, -1.0F, -HALF_THICKNESS, 0.0F, EDGE_UV);
        horizontalRim(consumer, pose, HALF_SIZE, 1.0F, -HALF_THICKNESS, 0.0F, EDGE_UV);
        horizontalRim(consumer, pose, -HALF_SIZE, -1.0F, HALF_THICKNESS, 1.0F - EDGE_UV, 1.0F);
        poseStack.pop();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public Identifier getTexture(RandomEventEntity entity) {
        var group = entity.group();
        return group == null ? EventRegistry.DEFAULT_TEXTURE : group.textureOrDefault();
    }

    private static void panel(VertexConsumer consumer, net.minecraft.client.util.math.MatrixStack.Entry pose, float z, float normalZ, float uZeroX) {
        var uOneX = -uZeroX;
        vertex(consumer, pose, uZeroX, HALF_SIZE, z, 0.0F, 0.0F, 0.0F, 0.0F, normalZ);
        vertex(consumer, pose, uZeroX, -HALF_SIZE, z, 0.0F, 1.0F, 0.0F, 0.0F, normalZ);
        vertex(consumer, pose, uOneX, -HALF_SIZE, z, 1.0F, 1.0F, 0.0F, 0.0F, normalZ);
        vertex(consumer, pose, uOneX, HALF_SIZE, z, 1.0F, 0.0F, 0.0F, 0.0F, normalZ);
    }

    private static void verticalRim(VertexConsumer consumer, net.minecraft.client.util.math.MatrixStack.Entry pose, float x, float normalX, float vZeroZ, float vZeroU, float vOneU) {
        var vOneZ = -vZeroZ;
        vertex(consumer, pose, x, HALF_SIZE, vZeroZ, vZeroU, 0.0F, normalX, 0.0F, 0.0F);
        vertex(consumer, pose, x, -HALF_SIZE, vZeroZ, vZeroU, 1.0F, normalX, 0.0F, 0.0F);
        vertex(consumer, pose, x, -HALF_SIZE, vOneZ, vOneU, 1.0F, normalX, 0.0F, 0.0F);
        vertex(consumer, pose, x, HALF_SIZE, vOneZ, vOneU, 0.0F, normalX, 0.0F, 0.0F);
    }

    private static void horizontalRim(VertexConsumer consumer, net.minecraft.client.util.math.MatrixStack.Entry pose, float y, float normalY, float uZeroZ, float vZero, float vOne) {
        var uOneZ = -uZeroZ;
        vertex(consumer, pose, -HALF_SIZE, y, uZeroZ, 0.0F, vZero, 0.0F, normalY, 0.0F);
        vertex(consumer, pose, -HALF_SIZE, y, uOneZ, 0.0F, vOne, 0.0F, normalY, 0.0F);
        vertex(consumer, pose, HALF_SIZE, y, uOneZ, 1.0F, vOne, 0.0F, normalY, 0.0F);
        vertex(consumer, pose, HALF_SIZE, y, uZeroZ, 1.0F, vZero, 0.0F, normalY, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, net.minecraft.client.util.math.MatrixStack.Entry pose, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
        net.per.primogemcraft.compat.client.Vertices.add(consumer, pose, x, y, z)
                .color(255, 255, 255, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
        net.per.primogemcraft.compat.client.Vertices.normal(consumer, pose, normalX, normalY, normalZ);
        consumer.next();
    }
}
