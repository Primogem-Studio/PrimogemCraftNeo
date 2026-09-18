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
import net.per.primogemcraft.entity.misc.WishEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class WishEntityModel extends EntityModel<WishEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MOD_ID, "wish_entity"), "main");

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

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(CORE, CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-HALF_SIZE, -SIZE, -HALF_SIZE, SIZE, SIZE, SIZE, CubeDeformation.NONE),
                PartPose.offset(0.0F, GROUND_LEVEL, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public void setupAnim(WishEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        core.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
