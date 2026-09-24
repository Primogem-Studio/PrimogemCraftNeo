package net.per.primogemcraft.client;

import net.minecraft.core.Position;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

import java.lang.reflect.Method;

public final class ZiplineRenderSpace {
    private static final ZiplineRenderSpace INSTANCE = new ZiplineRenderSpace();
    private Object helper;
    private Method containing;
    private Method renderPose;
    private Method transform;

    private ZiplineRenderSpace() {
        if (!ModList.get().isLoaded("sable")) return;
        try {
            helper = Class.forName("dev.ryanhcode.sable.Sable").getField("HELPER").get(null);
            containing = helper.getClass().getMethod("getContaining", Level.class, Position.class);
            renderPose = Class.forName("dev.ryanhcode.sable.sublevel.ClientSubLevel").getMethod("renderPose", float.class);
            transform = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc").getMethod("transformPosition", Vec3.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize Sable zipline rendering", exception);
        }
    }

    public static Vec3 position(Level level, Vec3 local, float partialTick) {
        if (INSTANCE.containing == null) return local;
        try {
            var subLevel = INSTANCE.containing.invoke(INSTANCE.helper, level, local);
            if (subLevel == null) return local;
            var pose = INSTANCE.renderPose.invoke(subLevel, partialTick);
            return (Vec3) INSTANCE.transform.invoke(pose, local);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot render zipline in Sable space", exception);
        }
    }

    public static Quaternionf rotation(Level level, Vec3 local, float partialTick) {
        var origin = position(level, local, partialTick);
        var x = position(level, local.add(1, 0, 0), partialTick).subtract(origin).toVector3f();
        var y = position(level, local.add(0, 1, 0), partialTick).subtract(origin).toVector3f();
        var z = position(level, local.add(0, 0, 1), partialTick).subtract(origin).toVector3f();
        return new Quaternionf().setFromNormalized(new Matrix3f().setColumn(0, x).setColumn(1, y).setColumn(2, z));
    }

    public static Vec3 entityOrigin(Entity entity, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
    }
}
