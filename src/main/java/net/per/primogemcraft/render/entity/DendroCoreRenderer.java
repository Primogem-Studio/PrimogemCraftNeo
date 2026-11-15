package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.entity.misc.DendroCoreEntity;
import net.per.primogemcraft.render.entity.model.DendroCoreModel;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DendroCoreRenderer extends EntityRenderer<DendroCoreEntity> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/dendro_core.png");
    private static final float SHADOW_RADIUS = 0.2F;
    private static final float MODEL_MIRROR = -1.0F;
    private static final float MODEL_GROUND_OFFSET = -1.501F;

    private final DendroCoreModel model;

    public DendroCoreRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = new DendroCoreModel(context.getPart(DendroCoreModel.LAYER));
        shadowRadius = SHADOW_RADIUS;
    }

    @Override
    public void render(DendroCoreEntity entity, float entityYaw, float partialTicks, net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumerProvider buffer, int packedLight) {
        poseStack.push();
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(MODEL_MIRROR, MODEL_MIRROR, 1.0F);
        poseStack.translate(0.0F, MODEL_GROUND_OFFSET, 0.0F);
        model.setAngles(entity, 0.0F, 0.0F, entity.age + partialTicks, 0.0F, 0.0F);
        model.render(poseStack, buffer.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE)), packedLight, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pop();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public Identifier getTexture(DendroCoreEntity entity) {
        return TEXTURE;
    }
}
