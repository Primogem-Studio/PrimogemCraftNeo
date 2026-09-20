package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ZiplineCarrierRenderer extends EntityRenderer<ZiplineCarrierEntity> {
    public ZiplineCarrierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(ZiplineCarrierEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.distanceToSqr(x, y, z) < 256 * 256;
    }

    @Override
    public void render(ZiplineCarrierEntity entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        if (!entity.moving()) return;
        var origin = entity.getPosition(partialTick);
        var source = entity.source().subtract(origin);
        var target = entity.target().subtract(origin);
        var normal = target.subtract(source).normalize();
        var buffer = buffers.getBuffer(RenderType.lines());
        buffer.addVertex(poses.last(), (float) source.x, (float) source.y, (float) source.z).setColor(235, 210, 100, 255)
                .setNormal(poses.last(), (float) normal.x, (float) normal.y, (float) normal.z);
        buffer.addVertex(poses.last(), (float) target.x, (float) target.y, (float) target.z).setColor(235, 210, 100, 255)
                .setNormal(poses.last(), (float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    public ResourceLocation getTextureLocation(ZiplineCarrierEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/zipline.png");
    }
}
