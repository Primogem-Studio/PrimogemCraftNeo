package net.per.primogemcraft.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.per.primogemcraft.entity.misc.ZiplineCarrierEntity;
import net.per.primogemcraft.entity.misc.ZiplineAnchorEntity;
import net.per.primogemcraft.client.ZiplineRenderSpace;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ZiplineCarrierRenderer extends EntityRenderer<ZiplineCarrierEntity> {
    private static final double CABLE_RADIUS = 0.025;

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
        var source = entity.combat() ? entity.source() : ZiplineRenderSpace.position(entity.level(),
                Vec3.atBottomCenterOf(entity.sourceBase()).add(0, ZiplineAnchorEntity.ANCHOR_HEIGHT, 0), partialTick);
        var target = entity.combat() ? entity.target() : ZiplineRenderSpace.position(entity.level(),
                Vec3.atBottomCenterOf(entity.targetBase()).add(0, ZiplineAnchorEntity.ANCHOR_HEIGHT, 0), partialTick);
        var direction = target.subtract(source);
        if (direction.lengthSqr() < 1.0E-8) return;
        direction = direction.normalize();
        var side = (Math.abs(direction.y) < 0.9
                ? new Vec3(-direction.z, 0, direction.x)
                : new Vec3(0, direction.z, -direction.y)).normalize().scale(CABLE_RADIUS);
        var perpendicular = direction.cross(side);
        var origin = ZiplineRenderSpace.entityOrigin(entity, partialTick);
        source = source.subtract(origin);
        target = target.subtract(origin);
        var buffer = buffers.getBuffer(RenderType.leash());
        var pose = poses.last();
        for (var corner = 0; corner <= 4; corner++) {
            var offset = (corner & 1) == 0 ? side : perpendicular;
            var sign = corner == 2 || corner == 3 ? -1 : 1;
            var x = offset.x * sign;
            var y = offset.y * sign;
            var z = offset.z * sign;
            buffer.addVertex(pose, (float) (source.x + x), (float) (source.y + y), (float) (source.z + z))
                    .setColor(235, 210, 100, 255).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, (float) (target.x + x), (float) (target.y + y), (float) (target.z + z))
                    .setColor(235, 210, 100, 255).setLight(LightTexture.FULL_BRIGHT);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ZiplineCarrierEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/zipline.png");
    }
}
