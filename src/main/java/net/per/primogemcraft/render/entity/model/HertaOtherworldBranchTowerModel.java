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
import net.per.primogemcraft.entity.misc.HertaOtherworldBranchTowerEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class HertaOtherworldBranchTowerModel extends EntityModel<HertaOtherworldBranchTowerEntity> {
    public static final EntityModelLayer LAYER =
            new EntityModelLayer(Identifier.of(MOD_ID, "herta_otherworld_branch_tower"), "main");

    private static final Dilation NO_DEFORMATION = new Dilation(0.0F);
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;

    private final ModelPart tower;

    public HertaOtherworldBranchTowerModel(ModelPart root) {
        tower = root.getChild("tower");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData mesh = new ModelData();
        ModelPartData root = mesh.getRoot();
        ModelPartData tower = root.addChild("tower", ModelPartBuilder.create()
                .uv(0, 17).cuboid(-4.0F, -2.0F, -4.0F, 9.0F, 2.0F, 9.0F, NO_DEFORMATION)
                .uv(0, 0).cuboid(-5.0F, -8.0F, -5.0F, 10.0F, 6.0F, 11.0F, NO_DEFORMATION)
                .uv(27, 19).cuboid(-4.0F, -11.0F, -4.0F, 8.0F, 3.0F, 9.0F, NO_DEFORMATION)
                .uv(26, 31).cuboid(0.0F, -10.0F, -3.0F, 6.0F, 4.0F, 7.0F, NO_DEFORMATION)
                .uv(19, 31).cuboid(-1.0F, -12.0F, 0.0F, 3.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .uv(0, 4).cuboid(6.0F, -8.0F, -5.0F, 1.0F, 6.0F, 1.0F, NO_DEFORMATION),
                ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        tower.addChild("antenna", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1.0F, -14.0F, 1.0F, 2.0F, 2.0F, 2.0F, NO_DEFORMATION),
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.3054F, -0.0873F));
        ModelPartData roof = tower.addChild("roof", ModelPartBuilder.create(),
                ModelTransform.of(-1.0F, 0.0F, -1.0F, -0.088F, 0.1304F, 0.0322F));
        roof.addChild("roof_ridge", ModelPartBuilder.create()
                .uv(31, 0).cuboid(2.0F, -13.0F, -2.0F, 5.0F, 1.0F, 5.0F, NO_DEFORMATION),
                ModelTransform.of(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
        roof.addChild("spire", ModelPartBuilder.create()
                .uv(0, 17).cuboid(-2.0F, -17.0F, 1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(27, 17).cuboid(1.0F, -17.0F, 1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(31, 6).cuboid(-1.0F, -16.0F, -1.0F, 3.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .uv(0, 23).cuboid(1.0F, -17.0F, -2.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 20).cuboid(-2.0F, -17.0F, -2.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION),
                ModelTransform.of(4.0F, 2.0F, -1.0F, -0.3927F, 0.0F, 0.0F));
        return TexturedModelData.of(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setAngles(HertaOtherworldBranchTowerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        tower.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
