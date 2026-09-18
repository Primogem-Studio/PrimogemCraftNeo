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
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class XiaoLanternModel extends EntityModel<XiaoLanternEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "xiao_lantern"), "main");

    private static final CubeDeformation NO_DEFORMATION = new CubeDeformation(0.0F);
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private final ModelPart lantern;

    public XiaoLanternModel(ModelPart root) {
        lantern = root.getChild("lantern");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition lantern = root.addOrReplaceChild("lantern", CubeListBuilder.create(), PartPose.offset(1.0F, 24.0F, -2.0F));
        PartDefinition body = lantern.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(60, 51).addBox(-1.0F, -18.0F, -16.0F, 18.0F, 1.0F, 18.0F, NO_DEFORMATION)
                .texOffs(20, 73).addBox(2.0F, -19.0F, -13.0F, 12.0F, 1.0F, 12.0F, NO_DEFORMATION)
                .texOffs(82, 70).addBox(3.0F, -21.0F, -12.0F, 10.0F, 2.0F, 10.0F, NO_DEFORMATION)
                .texOffs(56, 73).addBox(5.0F, -23.0F, -10.0F, 6.0F, 1.0F, 6.0F, NO_DEFORMATION)
                .texOffs(0, 0).addBox(7.0F, -27.0F, -8.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 51).addBox(-2.0F, -8.0F, -17.0F, 20.0F, 2.0F, 20.0F, NO_DEFORMATION)
                .texOffs(0, 29).addBox(-2.0F, -17.0F, -17.0F, 20.0F, 2.0F, 20.0F, NO_DEFORMATION)
                .texOffs(60, 29).addBox(-1.0F, -6.0F, -16.0F, 18.0F, 2.0F, 18.0F, NO_DEFORMATION)
                .texOffs(0, 0).addBox(-3.0F, -15.0F, -18.0F, 22.0F, 7.0F, 22.0F, NO_DEFORMATION)
                .texOffs(66, 0).addBox(0.0F, -4.0F, -15.0F, 16.0F, 1.0F, 16.0F, NO_DEFORMATION),
                PartPose.ZERO);
        PartDefinition rim = body.addOrReplaceChild("rim", CubeListBuilder.create()
                .texOffs(0, 12).addBox(7.0F, -25.0F, -4.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .texOffs(8, 6).addBox(7.0F, -25.0F, -12.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .texOffs(8, 0).addBox(3.0F, -25.0F, -8.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 6).addBox(11.0F, -25.0F, -8.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION),
                PartPose.ZERO);
        rim.addOrReplaceChild("rim_stud", CubeListBuilder.create()
                .texOffs(16, 8).addBox(2.0F, -23.0F, -8.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(16, 0).addBox(13.0F, -23.0F, -7.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(15, 11).addBox(7.0F, -23.0F, -2.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(15, 5).addBox(8.0F, -23.0F, -13.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION),
                PartPose.ZERO);
        lantern.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 39).addBox(5.0F, -3.0F, -3.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(8, 34).addBox(2.0F, -3.0F, -6.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 34).addBox(2.0F, -3.0F, -10.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(8, 29).addBox(5.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 29).addBox(9.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(6, 17).addBox(12.0F, -3.0F, -6.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(14, 15).addBox(12.0F, -3.0F, -10.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(8, 12).addBox(9.0F, -3.0F, -3.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .texOffs(0, 98).addBox(4.0F, -2.0F, -11.0F, 8.0F, 1.0F, 8.0F, NO_DEFORMATION),
                PartPose.ZERO);
        PartDefinition border = lantern.addOrReplaceChild("border", CubeListBuilder.create()
                .texOffs(72, 98).addBox(-1.0F, -16.0F, -19.0F, 18.0F, 7.0F, 1.0F, NO_DEFORMATION)
                .texOffs(88, 17).addBox(-1.0F, -16.0F, 4.0F, 18.0F, 7.0F, 1.0F, NO_DEFORMATION)
                .texOffs(0, 73).addBox(19.0F, -16.0F, -16.0F, 1.0F, 7.0F, 18.0F, NO_DEFORMATION)
                .texOffs(62, 70).addBox(-4.0F, -16.0F, -16.0F, 1.0F, 7.0F, 18.0F, NO_DEFORMATION),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition borderMiddle = border.addOrReplaceChild("border_middle", CubeListBuilder.create()
                .texOffs(102, 88).addBox(1.0F, -11.0F, -18.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(102, 85).addBox(1.0F, -11.0F, 3.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(56, 95).addBox(18.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION)
                .texOffs(40, 88).addBox(-3.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        borderMiddle.addOrReplaceChild("border_inner", CubeListBuilder.create()
                .texOffs(102, 82).addBox(1.0F, -11.0F, -18.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(88, 25).addBox(1.0F, -11.0F, 3.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .texOffs(86, 82).addBox(18.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION)
                .texOffs(24, 86).addBox(-3.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION),
                PartPose.offset(0.0F, -9.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(XiaoLanternEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        lantern.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
