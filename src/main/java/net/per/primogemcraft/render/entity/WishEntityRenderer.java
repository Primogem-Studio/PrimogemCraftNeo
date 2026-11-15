package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.render.entity.model.WishEntityModel;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class WishEntityRenderer extends EntityRenderer<WishEntity> {
    private static final Identifier CAPTURING_RADIANCE_TEXTURE = Identifier.of(MOD_ID, "textures/entity/wish_entity_capturing_radiance.png");
    private static final Identifier COLORFUL_TEXTURE = Identifier.of(MOD_ID, "textures/entity/wish_entity_colorful.png");
    private static final float BOB_AMPLITUDE = 0.06F;
    private static final float BOB_SPEED = 0.15F;
    private static final float MODEL_MIRROR = -1.0F;
    private static final float MODEL_GROUND_OFFSET = -1.6F;
    private static final int NO_TINT = -1;

    private final WishEntityModel model;

    public WishEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new WishEntityModel(context.getPart(WishEntityModel.LAYER_LOCATION));
    }

    @Override
    public void render(WishEntity entity, float entityYaw, float partialTicks, net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumerProvider buffer, int packedLight) {
        poseStack.push();
        if (entity.isOnGround())
            poseStack.translate(0.0F, MathHelper.sin((entity.getAge() + partialTicks) * BOB_SPEED) * BOB_AMPLITUDE, 0.0F);
        poseStack.scale(MODEL_MIRROR, MODEL_MIRROR, 1.0F);
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entityYaw));
        poseStack.translate(0.0F, MODEL_GROUND_OFFSET, 0.0F);
        model.render(poseStack, buffer.getBuffer(RenderLayer.getEntityCutoutNoCull(getTexture(entity))), net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pop();
    }

    @Override
    public Identifier getTexture(WishEntity entity) {
        if (entity.isRadianceVisible()) return CAPTURING_RADIANCE_TEXTURE;
        if (entity.isColorful()) return COLORFUL_TEXTURE;
        return entity.displayRarity().texture();
    }
}
