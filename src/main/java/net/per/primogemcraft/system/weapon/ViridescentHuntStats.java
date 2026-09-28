package net.per.primogemcraft.system.weapon;

public final class ViridescentHuntStats {
    public static final double TRIGGER_CHANCE = 0.5D;
    public static final double RADIUS = 3.0D;
    public static final int DURATION = 80;
    public static final int DAMAGE_INTERVAL = 10;

    private ViridescentHuntStats() {
    }

    public static double damageRatio(int refinement) {
        return 0.4D + 0.1D * (Math.max(1, refinement) - 1);
    }

    public static int cooldown(int refinement) {
        return Math.max(20, 280 - 20 * (Math.max(1, refinement) - 1));
    }
}
