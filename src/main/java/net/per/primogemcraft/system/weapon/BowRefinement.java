package net.per.primogemcraft.system.weapon;

public final class BowRefinement {
    private BowRefinement() {
    }

    public static double targetRange(int refinement) {
        return 8.0D + 2.0D * (Math.max(1, refinement) - 1);
    }

    public static double drawMovementBonus(int refinement) {
        return Math.max(1, refinement) - 1.0D;
    }
}
