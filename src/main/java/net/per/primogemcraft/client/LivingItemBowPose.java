package net.per.primogemcraft.client;

import org.joml.Quaternionf;

public final class LivingItemBowPose {
    private LivingItemBowPose() {
    }

    public static Quaternionf rotation(float yaw, float pitch) {
        return new Quaternionf().rotateY((float) Math.toRadians(-yaw)).rotateX((float) Math.toRadians(pitch))
                .rotateY((float) (Math.PI / 2)).rotateZ((float) (Math.PI / 4));
    }
}
