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
import net.per.primogemcraft.entity.misc.WishEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class WishEntityModel extends EntityModel<WishEntity> {
    public static final EntityModelLayer LAYER_LOCATION =
            new EntityModelLayer(Identifier.of(MOD_ID, "wish_entity"), "main");

    private static final String CORE = "core";
    private static final int TEXTURE_WIDTH = 32;
    private static final int TEXTURE_HEIGHT = 32;
    private static final float SIZE = 8.0F;
    private static final float HALF_SIZE = SIZE * 0.5F;
    private static final float GROUND_LEVEL = 24.0F;

    private final ModelPart core;

    public WishEntityModel(ModelPart root) {
        core = root.getChild(CORE);
    }

    public static TexturedModelData createBodyLayer() {
        ModelData mesh = new ModelData();
        ModelPartData root = mesh.getRoot();
        root.addChild(CORE, ModelPartBuilder.create()
                .uv(0, 0)
                        .cuboid(-HALF_SIZE, -SIZE, -HALF_SIZE, SIZE, SIZE, SIZE, Dilation.NONE),
                ModelTransform.pivot(0.0F, GROUND_LEVEL, 0.0F));
        return TexturedModelData.of(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setAngles(WishEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void render(net.minecraft.client.util.math.MatrixStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        core.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
