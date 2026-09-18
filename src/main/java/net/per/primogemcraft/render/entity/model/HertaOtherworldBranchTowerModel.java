package net.per.primogemcraft.render.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.entity.misc.HertaOtherworldBranchTowerEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class HertaOtherworldBranchTowerModel extends EntityModel<HertaOtherworldBranchTowerEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "herta_otherworld_branch_tower"), "main");

    private static final CubeDeformation NO_DEFORMATION = new CubeDeformation(0.0F);
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;

    private final ModelPart tower;

    public HertaOtherworldBranchTowerModel(ModelPart root) {
        tower = root.getChild("tower");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition tower = root.addOrReplaceChild("tower", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-4.0F, -2.0F, -4.0F, 9.0F, 2.0F, 9.0F, NO_DEFORMATION)
                .texOffs(0, 0).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 6.0F, 11.0F, NO_DEFORMATION)
                .texOffs(27, 19).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 3.0F, 9.0F, NO_DEFORMATION)
                .texOffs(26, 31).addBox(0.0F, -10.0F, -3.0F, 6.0F, 4.0F, 7.0F, NO_DEFORMATION)
                .texOffs(19, 31).addBox(-1.0F, -12.0F, 0.0F, 3.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .texOffs(0, 4).addBox(6.0F, -8.0F, -5.0F, 1.0F, 6.0F, 1.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        tower.addOrReplaceChild("antenna", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, -14.0F, 1.0F, 2.0F, 2.0F, 2.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3054F, -0.0873F));
        PartDefinition roof = tower.addOrReplaceChild("roof", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -1.0F, -0.088F, 0.1304F, 0.0322F));
        roof.addOrReplaceChild("roof_ridge", CubeListBuilder.create()
                        .texOffs(31, 0).addBox(2.0F, -13.0F, -2.0F, 5.0F, 1.0F, 5.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3927F, 0.0F, 0.0F));
        roof.addOrReplaceChild("spire", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-2.0F, -17.0F, 1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(27, 17).addBox(1.0F, -17.0F, 1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(31, 6).addBox(-1.0F, -16.0F, -1.0F, 3.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .texOffs(0, 23).addBox(1.0F, -17.0F, -2.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 20).addBox(-2.0F, -17.0F, -2.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION),
                PartPose.offsetAndRotation(4.0F, 2.0F, -1.0F, -0.3927F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(HertaOtherworldBranchTowerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        tower.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
