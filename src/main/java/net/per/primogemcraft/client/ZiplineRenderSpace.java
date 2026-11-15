package net.per.primogemcraft.client;

import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

import java.lang.reflect.Method;

/**
 * 滑索渲染空间变换（Sable 兼容）。
 * <p>通过反射调用 Sable 的子世界渲染变换；未加载 Sable 时退化为恒等变换。
 */
public final class ZiplineRenderSpace {
    private static final ZiplineRenderSpace INSTANCE = new ZiplineRenderSpace();
    private Object helper;
    private Method containing;
    private Method renderPose;
    private Method transform;

    private ZiplineRenderSpace() {
        if (!FabricLoader.getInstance().isModLoaded("sable")) return;
        try {
            helper = Class.forName("dev.ryanhcode.sable.Sable").getField("HELPER").get(null);
            containing = helper.getClass().getMethod("getContaining", World.class, net.minecraft.util.math.Position.class);
            renderPose = Class.forName("dev.ryanhcode.sable.sublevel.ClientSubLevel").getMethod("renderPose", float.class);
            transform = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc").getMethod("transformPosition", Vec3d.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize Sable zipline rendering", exception);
        }
    }

    public static Vec3d position(World world, Vec3d local, float partialTick) {
        if (INSTANCE.containing == null) return local;
        try {
            var subLevel = INSTANCE.containing.invoke(INSTANCE.helper, world, local);
            if (subLevel == null) return local;
            var pose = INSTANCE.renderPose.invoke(subLevel, partialTick);
            return (Vec3d) INSTANCE.transform.invoke(pose, local);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot render zipline in Sable space", exception);
        }
    }

    public static Quaternionf rotation(World world, Vec3d local, float partialTick) {
        var origin = position(world, local, partialTick);
        var x = position(world, local.add(1, 0, 0), partialTick).subtract(origin).toVector3f();
        var y = position(world, local.add(0, 1, 0), partialTick).subtract(origin).toVector3f();
        var z = position(world, local.add(0, 0, 1), partialTick).subtract(origin).toVector3f();
        return new Quaternionf().setFromNormalized(new Matrix3f().setColumn(0, x).setColumn(1, y).setColumn(2, z));
    }

    public static Vec3d entityOrigin(Entity entity, float partialTick) {
        return new Vec3d(MathHelper.lerp(partialTick, entity.prevX, entity.getX()),
                MathHelper.lerp(partialTick, entity.prevY, entity.getY()),
                MathHelper.lerp(partialTick, entity.prevZ, entity.getZ()));
    }
}
