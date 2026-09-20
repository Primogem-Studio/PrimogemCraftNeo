package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.per.primogemcraft.client.ZiplineModel;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ZiplineAnchorRenderer extends EntityRenderer<ZiplineAnchorEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/zipline.png");

    public ZiplineAnchorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(ZiplineAnchorEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z) && frustum.isVisible(new AABB(
                entity.getX() - 2.2, entity.getY(), entity.getZ() - 2.2,
                entity.getX() + 2.2, entity.getY() + 6.5, entity.getZ() + 2.2));
    }

    @Override
    public void render(ZiplineAnchorEntity entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(yaw));
        var scale = (float) ZiplineAnchorEntity.MODEL_SCALE / 16;
        poses.scale(scale, scale, scale);
        poses.translate(-8, 0, -8);
        ZiplineModel.INSTANCE.render(poses, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(ZiplineAnchorEntity entity) {
        return TEXTURE;
    }
}
