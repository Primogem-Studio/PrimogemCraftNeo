package net.per.primogemcraft.system.weapon;

public final class ThunderingPulseStats {
    private ThunderingPulseStats() {
    }

    public static int duration(int refinement) {
        return 100 + 25 * (Math.max(1, refinement) - 1);
    }

    public static int empowermentCooldown(int refinement) {
        return Math.max(0, 600 - 100 * (Math.max(1, refinement) - 1));
    }

    public static int maximumPiercing(int refinement) {
        return Math.max(1, refinement) + 1;
    }

    public static int dashCooldown(int refinement) {
        return Math.max(10, 200 - 40 * (Math.max(1, refinement) - 1));
    }

    public static double dashSpeed(int refinement) {
        return 1.5D + 0.25D * (Math.max(1, refinement) - 1);
    }
}
