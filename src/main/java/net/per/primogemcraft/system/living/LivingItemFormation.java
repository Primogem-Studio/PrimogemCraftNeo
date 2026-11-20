package net.per.primogemcraft.system.living;

import net.minecraft.util.math.Vec3d;

public enum LivingItemFormation {
    NONE, RING, LINE, WEDGE;

    /** Returns an owner's horizontal formation slot, bounded to stay within command reach. */
    public Vec3d offset(int index, int count, double spacing, Vec3d direction) {
        var forward = direction.multiply(1, 0, 1).normalize();
        if (forward.lengthSquared() < 1.0E-6) forward = new Vec3d(0, 0, 1);
        var right = new Vec3d(forward.z, 0, -forward.x);
        return switch (this) {
            case NONE -> Vec3d.ZERO;
            case RING -> {
                var angle = Math.PI * 2 * index / Math.max(1, count);
                var radius = Math.max(2, Math.min(8, count * 0.3)) * spacing;
                yield right.multiply(Math.cos(angle) * radius).add(forward.multiply(Math.sin(angle) * radius));
            }
            case LINE -> right.multiply((index - (count - 1) / 2.0) * Math.min(spacing, 24.0 / Math.max(1, count - 1)))
                    .add(forward.multiply(2));
            case WEDGE -> {
                var row = (index + 1) / 2;
                var step = Math.min(spacing, 10.0 / Math.max(1, count / 2));
                yield right.multiply((index % 2 == 0 ? 1 : -1) * row * step).add(forward.multiply(2 - row * step));
            }
        };
    }
}
