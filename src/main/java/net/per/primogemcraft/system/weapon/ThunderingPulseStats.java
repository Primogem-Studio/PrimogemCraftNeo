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

    public static float empowermentDamageBonus(int refinement) {
        return 0.5F + 0.125F * (Math.max(1, refinement) - 1);
    }

    public static int dashCooldown(int refinement) {
        return Math.max(10, 200 - 40 * (Math.max(1, refinement) - 1));
    }

    public static double dashSpeed(int refinement) {
        return 1.5D + 0.25D * (Math.max(1, refinement) - 1);
    }

    public static int fireArrowChance(int refinement) {
        return refinement < 5 ? 0 : 50 + 5 * (Math.min(8, refinement) - 5);
    }

    public static double fireArrowDamageMultiplier(int refinement) {
        return refinement < 5 ? 0.0D : (60 + 10 * (Math.min(8, refinement) - 5)) / 100.0D;
    }
}
