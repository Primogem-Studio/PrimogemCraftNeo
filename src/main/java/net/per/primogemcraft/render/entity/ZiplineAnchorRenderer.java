package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.client.ZiplineModel;
import net.per.primogemcraft.client.ZiplineRenderSpace;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ZiplineAnchorRenderer extends EntityRenderer<ZiplineAnchorEntity> {
    private static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/zipline.png");

    public ZiplineAnchorRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(ZiplineAnchorEntity entity, Frustum frustum, double x, double y, double z) {
        var local = Vec3d.ofBottomCenter(entity.basePosition());
        var previous = entity.temporary() ? entity.getPos() : ZiplineRenderSpace.position(entity.getWorld(), local, 0);
        var current = entity.temporary() ? entity.getPos() : ZiplineRenderSpace.position(entity.getWorld(), local, 1);
        return entity.shouldRender(Math.min(previous.squaredDistanceTo(x, y, z), current.squaredDistanceTo(x, y, z)))
                && frustum.isVisible(new Box(previous, current).expand(8));
    }

    @Override
    public void render(ZiplineAnchorEntity entity, float yaw, float partialTick, net.minecraft.client.util.math.MatrixStack poses, VertexConsumerProvider buffers, int light) {
        poses.push();
        if (!entity.temporary()) {
            var local = Vec3d.ofBottomCenter(entity.basePosition());
            var position = ZiplineRenderSpace.position(entity.getWorld(), local, partialTick);
            var offset = position.subtract(ZiplineRenderSpace.entityOrigin(entity, partialTick));
            poses.translate(offset.x, offset.y, offset.z);
            poses.multiply(ZiplineRenderSpace.rotation(entity.getWorld(), local, partialTick));
        }
        poses.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        var scale = (float) ZiplineAnchorEntity.MODEL_SCALE / 16;
        poses.scale(scale, scale, scale);
        poses.translate(-8, 0, -8);
        ZiplineModel.INSTANCE.render(poses, buffers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE)), light);
        poses.pop();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public Identifier getTexture(ZiplineAnchorEntity entity) {
        return TEXTURE;
    }
}
