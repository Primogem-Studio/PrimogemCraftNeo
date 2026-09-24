package net.per.primogemcraft.system.zipline;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.per.primogemcraft.PrimogemCraft;

import java.lang.reflect.Method;

public final class ZiplineSpace {
    private static final ZiplineSpace INSTANCE = new ZiplineSpace();
    private Object helper;
    private Method project;

    private ZiplineSpace() {
        if (!ModList.get().isLoaded("sable")) return;
        try {
            var sable = Class.forName("dev.ryanhcode.sable.Sable");
            helper = sable.getField("HELPER").get(null);
            project = helper.getClass().getMethod("projectOutOfSubLevel", Level.class, Vec3.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot initialize Sable zipline compatibility", exception);
        }
        PrimogemCraft.LOGGER.info("Enabled Sable zipline compatibility");
    }

    public static Vec3 worldPosition(Level level, Vec3 position) {
        if (INSTANCE.project == null) return position;
        try {
            return (Vec3) INSTANCE.project.invoke(INSTANCE.helper, level, position);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot transform zipline position through Sable", exception);
        }
    }
}
