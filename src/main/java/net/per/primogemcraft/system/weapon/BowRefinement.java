package net.per.primogemcraft.system.weapon;

public final class BowRefinement {
    private BowRefinement() {
    }

    public static double targetRange(int refinement) {
        return 8.0D + 2.0D * (Math.max(1, refinement) - 1);
    }

    public static double slingshotDamageBonus(int refinement) {
        return switch (Math.max(1, refinement)) {
            case 1 -> 0.32D;
            case 2 -> 0.36D;
            default -> 0.48D + 0.06D * (refinement - 3);
        };
    }

    public static double drawMovementBonus(int refinement) {
        return Math.max(1, refinement) - 1.0D;
    }
}
