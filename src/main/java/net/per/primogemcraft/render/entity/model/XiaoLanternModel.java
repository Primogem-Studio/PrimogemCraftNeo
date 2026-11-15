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
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class XiaoLanternModel extends EntityModel<XiaoLanternEntity> {
    public static final EntityModelLayer LAYER =
            new EntityModelLayer(Identifier.of(MOD_ID, "xiao_lantern"), "main");

    private static final Dilation NO_DEFORMATION = new Dilation(0.0F);
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private final ModelPart lantern;

    public XiaoLanternModel(ModelPart root) {
        lantern = root.getChild("lantern");
    }

    public static TexturedModelData createBodyLayer() {
        ModelData mesh = new ModelData();
        ModelPartData root = mesh.getRoot();
        ModelPartData lantern = root.addChild("lantern", ModelPartBuilder.create(), ModelTransform.pivot(1.0F, 24.0F, -2.0F));
        ModelPartData body = lantern.addChild("body", ModelPartBuilder.create()
                .uv(60, 51).cuboid(-1.0F, -18.0F, -16.0F, 18.0F, 1.0F, 18.0F, NO_DEFORMATION)
                .uv(20, 73).cuboid(2.0F, -19.0F, -13.0F, 12.0F, 1.0F, 12.0F, NO_DEFORMATION)
                .uv(82, 70).cuboid(3.0F, -21.0F, -12.0F, 10.0F, 2.0F, 10.0F, NO_DEFORMATION)
                .uv(56, 73).cuboid(5.0F, -23.0F, -10.0F, 6.0F, 1.0F, 6.0F, NO_DEFORMATION)
                .uv(0, 0).cuboid(7.0F, -27.0F, -8.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 51).cuboid(-2.0F, -8.0F, -17.0F, 20.0F, 2.0F, 20.0F, NO_DEFORMATION)
                .uv(0, 29).cuboid(-2.0F, -17.0F, -17.0F, 20.0F, 2.0F, 20.0F, NO_DEFORMATION)
                .uv(60, 29).cuboid(-1.0F, -6.0F, -16.0F, 18.0F, 2.0F, 18.0F, NO_DEFORMATION)
                .uv(0, 0).cuboid(-3.0F, -15.0F, -18.0F, 22.0F, 7.0F, 22.0F, NO_DEFORMATION)
                .uv(66, 0).cuboid(0.0F, -4.0F, -15.0F, 16.0F, 1.0F, 16.0F, NO_DEFORMATION),
                ModelTransform.NONE);
        ModelPartData rim = body.addChild("rim", ModelPartBuilder.create()
                .uv(0, 12).cuboid(7.0F, -25.0F, -4.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .uv(8, 6).cuboid(7.0F, -25.0F, -12.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .uv(8, 0).cuboid(3.0F, -25.0F, -8.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 6).cuboid(11.0F, -25.0F, -8.0F, 2.0F, 4.0F, 2.0F, NO_DEFORMATION),
                ModelTransform.NONE);
        rim.addChild("rim_stud", ModelPartBuilder.create()
                .uv(16, 8).cuboid(2.0F, -23.0F, -8.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(16, 0).cuboid(13.0F, -23.0F, -7.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(15, 11).cuboid(7.0F, -23.0F, -2.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(15, 5).cuboid(8.0F, -23.0F, -13.0F, 1.0F, 2.0F, 1.0F, NO_DEFORMATION),
                ModelTransform.NONE);
        lantern.addChild("base", ModelPartBuilder.create()
                .uv(0, 39).cuboid(5.0F, -3.0F, -3.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(8, 34).cuboid(2.0F, -3.0F, -6.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 34).cuboid(2.0F, -3.0F, -10.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(8, 29).cuboid(5.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 29).cuboid(9.0F, -3.0F, -13.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(6, 17).cuboid(12.0F, -3.0F, -6.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(14, 15).cuboid(12.0F, -3.0F, -10.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(8, 12).cuboid(9.0F, -3.0F, -3.0F, 2.0F, 3.0F, 2.0F, NO_DEFORMATION)
                .uv(0, 98).cuboid(4.0F, -2.0F, -11.0F, 8.0F, 1.0F, 8.0F, NO_DEFORMATION),
                ModelTransform.NONE);
        ModelPartData border = lantern.addChild("border", ModelPartBuilder.create()
                .uv(72, 98).cuboid(-1.0F, -16.0F, -19.0F, 18.0F, 7.0F, 1.0F, NO_DEFORMATION)
                .uv(88, 17).cuboid(-1.0F, -16.0F, 4.0F, 18.0F, 7.0F, 1.0F, NO_DEFORMATION)
                .uv(0, 73).cuboid(19.0F, -16.0F, -16.0F, 1.0F, 7.0F, 18.0F, NO_DEFORMATION)
                .uv(62, 70).cuboid(-4.0F, -16.0F, -16.0F, 1.0F, 7.0F, 18.0F, NO_DEFORMATION),
                ModelTransform.pivot(0.0F, 1.0F, 0.0F));
        ModelPartData borderMiddle = border.addChild("border_middle", ModelPartBuilder.create()
                .uv(102, 88).cuboid(1.0F, -11.0F, -18.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(102, 85).cuboid(1.0F, -11.0F, 3.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(56, 95).cuboid(18.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION)
                .uv(40, 88).cuboid(-3.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION),
                ModelTransform.pivot(0.0F, 2.0F, 0.0F));
        borderMiddle.addChild("border_inner", ModelPartBuilder.create()
                .uv(102, 82).cuboid(1.0F, -11.0F, -18.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(88, 25).cuboid(1.0F, -11.0F, 3.0F, 14.0F, 2.0F, 1.0F, NO_DEFORMATION)
                .uv(86, 82).cuboid(18.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION)
                .uv(24, 86).cuboid(-3.0F, -11.0F, -14.0F, 1.0F, 2.0F, 14.0F, NO_DEFORMATION),
                ModelTransform.pivot(0.0F, -9.0F, 0.0F));
        return TexturedModelData.of(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setAngles(XiaoLanternEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        lantern.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
