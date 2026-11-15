package net.per.primogemcraft.render.entity;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.client.ZiplineRenderSpace;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ZiplineCarrierRenderer extends EntityRenderer<ZiplineCarrierEntity> {
    private static final double CABLE_RADIUS = 0.025;

    public ZiplineCarrierRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(ZiplineCarrierEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.squaredDistanceTo(x, y, z) < 256 * 256;
    }

    @Override
    public void render(ZiplineCarrierEntity entity, float yaw, float partialTick, net.minecraft.client.util.math.MatrixStack poses, VertexConsumerProvider buffers, int light) {
        if (!entity.moving()) return;
        var source = entity.combat() ? entity.source() : ZiplineRenderSpace.position(entity.getWorld(),
                Vec3d.ofBottomCenter(entity.sourceBase()).add(0, ZiplineAnchorEntity.ANCHOR_HEIGHT, 0), partialTick);
        var target = entity.combat() ? entity.target() : ZiplineRenderSpace.position(entity.getWorld(),
                Vec3d.ofBottomCenter(entity.targetBase()).add(0, ZiplineAnchorEntity.ANCHOR_HEIGHT, 0), partialTick);
        var direction = target.subtract(source);
        if (direction.lengthSquared() < 1.0E-8) return;
        direction = direction.normalize();
        var side = (Math.abs(direction.y) < 0.9
                ? new Vec3d(-direction.z, 0, direction.x)
                : new Vec3d(0, direction.z, -direction.y)).normalize().multiply(CABLE_RADIUS);
        var perpendicular = direction.crossProduct(side);
        var origin = ZiplineRenderSpace.entityOrigin(entity, partialTick);
        source = source.subtract(origin);
        target = target.subtract(origin);
        var buffer = buffers.getBuffer(RenderLayer.getLeash());
        var pose = poses.peek();
        for (var corner = 0; corner <= 4; corner++) {
            var offset = (corner & 1) == 0 ? side : perpendicular;
            var sign = corner == 2 || corner == 3 ? -1 : 1;
            var x = offset.x * sign;
            var y = offset.y * sign;
            var z = offset.z * sign;
            net.per.primogemcraft.compat.client.Vertices.add(buffer, pose, (float) (source.x + x), (float) (source.y + y), (float) (source.z + z))
                    .color(235, 210, 100, 255).light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
            buffer.next();
            net.per.primogemcraft.compat.client.Vertices.add(buffer, pose, (float) (target.x + x), (float) (target.y + y), (float) (target.z + z))
                    .color(235, 210, 100, 255).light(net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
            buffer.next();
        }
    }

    @Override
    public Identifier getTexture(ZiplineCarrierEntity entity) {
        return Identifier.of(MOD_ID, "textures/entity/zipline.png");
    }
}
