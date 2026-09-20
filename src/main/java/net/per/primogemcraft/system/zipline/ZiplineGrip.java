package net.per.primogemcraft.system.zipline;

import net.minecraft.world.phys.Vec3;

public final class ZiplineGrip {
    public static final float ARM_ROLL = -1.1F;
    private static final double MODEL_SCALE = 0.9375;

    public static Vec3 handOffset(boolean slim, float scale, float bodyYaw) {
        var palmX = slim ? -0.5 : -1.0;
        var palmY = -9.5;
        var cosine = Math.cos(ARM_ROLL);
        var sine = Math.sin(ARM_ROLL);
        var horizontal = (-5 + palmX * cosine - palmY * sine) / 16;
        var vertical = 1.501 - (2 + palmX * sine + palmY * cosine) / 16;
        var yaw = Math.toRadians(bodyYaw);
        return new Vec3(horizontal * Math.cos(yaw), vertical, horizontal * Math.sin(yaw)).scale(MODEL_SCALE * scale);
    }

    public static float facingYaw(Vec3 direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
    }

    public static float modelYaw(float bodyYaw) {
        return -bodyYaw - 90.0F;
    }
}
