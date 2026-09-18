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
import net.per.primogemcraft.entity.misc.DendroCoreEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class DendroCoreModel extends EntityModel<DendroCoreEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "dendro_core"), "main");

    private static final CubeDeformation NO_DEFORMATION = new CubeDeformation(0.0F);
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 32;
    private static final float SPIN_SPEED = 0.06F;

    private final ModelPart core;

    public DendroCoreModel(ModelPart root) {
        core = root.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 4.0F, 6.0F, NO_DEFORMATION)
                .texOffs(13, 22).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(18, 0).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 15).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .texOffs(12, 11).addBox(-2.0F, -5.0F, -2.0F, 4.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .texOffs(5, 21).addBox(-2.0F, -5.0F, 2.0F, 4.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(18, 3).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(14, 17).addBox(2.0F, -5.0F, -2.0F, 1.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .texOffs(8, 16).addBox(-3.0F, -5.0F, -2.0F, 1.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .texOffs(0, 10).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 1.0F, 4.0F, NO_DEFORMATION)
                .texOffs(22, 8).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(12, 27).addBox(2.0F, -6.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(12, 12).addBox(-1.0F, -6.0F, 2.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(12, 10).addBox(-1.0F, 0.0F, 2.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 12).addBox(-1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 10).addBox(-1.0F, 2.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 24).addBox(-3.0F, -2.0F, -4.0F, 3.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(19, 22).addBox(0.0F, -2.0F, 3.0F, 3.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(12, 25).addBox(-3.0F, -2.0F, 3.0F, 2.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(24, 20).addBox(1.0F, -2.0F, -4.0F, 2.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(20, 16).addBox(-4.0F, -2.0F, 0.0F, 1.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .texOffs(24, 11).addBox(-4.0F, -2.0F, -3.0F, 1.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(24, 5).addBox(3.0F, -2.0F, 1.0F, 1.0F, 1.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 20).addBox(3.0F, -2.0F, -3.0F, 1.0F, 1.0F, 3.0F, NO_DEFORMATION)
                .texOffs(0, 0).addBox(-1.0F, -5.0F, 3.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION)
                .texOffs(14, 18).addBox(0.0F, -6.0F, -3.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(14, 16).addBox(-3.0F, -6.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(8, 15).addBox(-3.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 15).addBox(-2.0F, 1.0F, -1.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(8, 23).addBox(-4.0F, -5.0F, -1.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION)
                .texOffs(20, 24).addBox(0.0F, -5.0F, -4.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION)
                .texOffs(4, 26).addBox(2.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 26).addBox(1.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(25, 16).addBox(0.0F, 2.0F, 0.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(8, 17).addBox(0.0F, 1.0F, -2.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 17).addBox(0.0F, 0.0F, -3.0F, 1.0F, 1.0F, 1.0F, NO_DEFORMATION)
                .texOffs(24, 24).addBox(3.0F, -5.0F, 0.0F, 1.0F, 5.0F, 1.0F, NO_DEFORMATION),
                PartPose.offset(0.0F, 20.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(DendroCoreEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        core.yRot = ageInTicks * SPIN_SPEED;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        core.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
