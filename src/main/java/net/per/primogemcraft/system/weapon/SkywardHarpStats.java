package net.per.primogemcraft.system.weapon;

public final class SkywardHarpStats {
    private SkywardHarpStats() {
    }

    public static double splitChance(int refinement) {
        return Math.clamp((refinement - 1) * 0.25D, 0.0D, 1.0D);
    }

    public static int splitCount(int refinement) {
        return refinement < 2 ? 0 : refinement < 5 ? 1 : 2 + (refinement - 5) / 2;
    }

    public static int vortexCooldown(int refinement) {
        return Math.max(100, 600 - 100 * (Math.max(1, refinement) - 1));
    }

    public static int vortexDuration(int refinement) {
        return refinement < 5 ? 200 : 400 + 100 * (refinement - 5);
    }

    public static double vortexRadius(int refinement) {
        return 10.0D + 2.0D * (Math.max(1, refinement) - 1);
    }
}
