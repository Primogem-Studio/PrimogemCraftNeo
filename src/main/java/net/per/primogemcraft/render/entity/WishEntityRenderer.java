package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.render.entity.model.WishEntityModel;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class WishEntityRenderer extends EntityRenderer<WishEntity> {
    private static final ResourceLocation CAPTURING_RADIANCE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/wish_entity_capturing_radiance.png");
    private static final ResourceLocation COLORFUL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/wish_entity_colorful.png");
    private static final float BOB_AMPLITUDE = 0.06F;
    private static final float BOB_SPEED = 0.15F;
    private static final float MODEL_MIRROR = -1.0F;
    private static final float MODEL_GROUND_OFFSET = -1.6F;
    private static final int NO_TINT = -1;

    private final WishEntityModel model;

    public WishEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new WishEntityModel(context.bakeLayer(WishEntityModel.LAYER_LOCATION));
    }

    @Override
    public void render(WishEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        if (entity.onGround())
            poseStack.translate(0.0F, Mth.sin((entity.tickCount + partialTicks) * BOB_SPEED) * BOB_AMPLITUDE, 0.0F);
        poseStack.scale(MODEL_MIRROR, MODEL_MIRROR, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        poseStack.translate(0.0F, MODEL_GROUND_OFFSET, 0.0F);
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, NO_TINT);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(WishEntity entity) {
        if (entity.isRadianceVisible()) return CAPTURING_RADIANCE_TEXTURE;
        if (entity.isColorful()) return COLORFUL_TEXTURE;
        return entity.displayRarity().texture();
    }
}
