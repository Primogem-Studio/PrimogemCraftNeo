package net.per.primogemcraft.system.zipline;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.per.primogemcraft.PrimogemCraft;

import java.lang.reflect.Method;

public final class ZiplineSpace {
    private static final ZiplineSpace INSTANCE = new ZiplineSpace();
    private Object helper;
    private Method project;

    private ZiplineSpace() {
        if (!FabricLoader.getInstance().isModLoaded("sable")) return;
        try {
            var sable = Class.forName("dev.ryanhcode.sable.Sable");
            helper = sable.getField("HELPER").get(null);
            project = helper.getClass().getMethod("projectOutOfSubLevel", World.class, Vec3d.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize Sable zipline compatibility", exception);
        }
        PrimogemCraft.LOGGER.info("Enabled Sable zipline compatibility");
    }

    public static Vec3d pos(World level, Vec3d position) {
        if (INSTANCE.project == null) return position;
        try {
            return (Vec3d) INSTANCE.project.invoke(INSTANCE.helper, level, position);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot transform zipline position through Sable", exception);
        }
    }
}
