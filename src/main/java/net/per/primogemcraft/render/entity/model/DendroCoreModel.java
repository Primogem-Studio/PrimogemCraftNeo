package net.per.primogemcraft.render.entity.model;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.util.Identifier;
import net.per.primogemcraft.entity.misc.DendroCoreEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DendroCoreModel extends EntityModel<DendroCoreEntity> {
    public static final EntityModelLayer LAYER =
            new EntityModelLayer(Identifier.of(MOD_ID, "dendro_core"), "main");

    private static final Dilation NO_DEFORMATION = new Dilation(0.0F);
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 32;
    private static final float SPIN_SPEED = 0.06F;

    private final ModelPart core;

    public DendroCoreModel(ModelPart root) {
        core = root.getChild("core");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData mesh = new ModelData();
        ModelPartData root = mesh.getRoot();
        root.addChild("core", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-3.0F, -4.0F, -3.0F, 6.0F, 4.0F, 6.0F, NO_DEFORMATION)
                .uv(13, 22).cuboid(-1.0F, 1.0F, -1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(18, 0).cuboid(-2.0F, 0.0F, -1.0F, 4.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 15).cuboid(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .uv(12, 11).cuboid(-2.0F, -5.0F, -2.0F, 4.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .uv(5, 21).cuboid(-2.0F, -5.0F, 2.0F, 4.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(18, 3).cuboid(-2.0F, -5.0F, -3.0F, 4.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(14, 17).cuboid(2.0F, -5.0F, -2.0F, 1.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .uv(8, 16).cuboid(-3.0F, -5.0F, -2.0F, 1.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .uv(0, 10).cuboid(-2.0F, -6.0F, -2.0F, 4.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .uv(22, 8).cuboid(-1.0F, -7.0F, -1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(12, 27).cuboid(2.0F, -6.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(12, 12).cuboid(-1.0F, -6.0F, 2.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(12, 10).cuboid(-1.0F, 0.0F, 2.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 12).cuboid(-1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 10).cuboid(-1.0F, 2.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 24).cuboid(-3.0F, -2.0F, -4.0F, 3.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(19, 22).cuboid(0.0F, -2.0F, 3.0F, 3.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(12, 25).cuboid(-3.0F, -2.0F, 3.0F, 2.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(24, 20).cuboid(1.0F, -2.0F, -4.0F, 2.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(20, 16).cuboid(-4.0F, -2.0F, 0.0F, 1.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .uv(24, 11).cuboid(-4.0F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(24, 5).cuboid(3.0F, -2.0F, 1.0F, 1.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 20).cuboid(3.0F, -2.0F, -3.0F, 1.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .uv(0, 0).cuboid(-1.0F, -5.0F, 3.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION)
                .uv(14, 18).cuboid(0.0F, -6.0F, -3.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(14, 16).cuboid(-3.0F, -6.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(8, 15).cuboid(-3.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 15).cuboid(-2.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(8, 23).cuboid(-4.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION)
                .uv(20, 24).cuboid(0.0F, -5.0F, -4.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION)
                .uv(4, 26).cuboid(2.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 26).cuboid(1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(25, 16).cuboid(0.0F, 2.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(8, 17).cuboid(0.0F, 1.0F, -2.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 17).cuboid(0.0F, 0.0F, -3.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .uv(24, 24).cuboid(3.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION),
                ModelTransform.pivot(0.0F, 20.0F, 0.0F));
        return TexturedModelData.of(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setAngles(DendroCoreEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        core.yaw = ageInTicks * SPIN_SPEED;
    }

    @Override
    public void render(net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
