package net.per.primogemcraft.system.living;

import net.minecraft.world.phys.Vec3;

public enum LivingItemFormation {
    NONE, RING, LINE, WEDGE;

    /** Returns an owner's horizontal formation slot, bounded to stay within command reach. */
    public Vec3 offset(int index, int count, double spacing, Vec3 direction) {
        var forward = direction.multiply(1, 0, 1).normalize();
        if (forward.lengthSqr() < 1.0E-6) forward = new Vec3(0, 0, 1);
        var right = new Vec3(forward.z, 0, -forward.x);
        return switch (this) {
            case NONE -> Vec3.ZERO;
            case RING -> {
                var angle = Math.PI * 2 * index / Math.max(1, count);
                var radius = Math.max(2, Math.min(8, count * 0.3)) * spacing;
                yield right.scale(Math.cos(angle) * radius).add(forward.scale(Math.sin(angle) * radius));
            }
            case LINE -> right.scale((index - (count - 1) / 2.0) * Math.min(spacing, 24.0 / Math.max(1, count - 1)))
                    .add(forward.scale(2));
            case WEDGE -> {
                var row = (index + 1) / 2;
                var step = Math.min(spacing, 10.0 / Math.max(1, count / 2));
                yield right.scale((index % 2 == 0 ? 1 : -1) * row * step).add(forward.scale(2 - row * step));
            }
        };
    }
}
