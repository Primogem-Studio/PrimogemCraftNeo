import net.per.primogemcraft.system.weapon.BowRefinement;

public final class BowRefinementTest {
    public static void main(String[] args) {
        var ranges = new double[]{8, 10, 12, 14, 16, 18, 20, 22};
        var movement = new double[]{0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6};
        for (var refinement = 1; refinement <= 8; refinement++) {
            check(BowRefinement.targetRange(refinement) == ranges[refinement - 1]);
            check(Math.abs(0.2D * (1.0D + BowRefinement.drawMovementBonus(refinement)) - movement[refinement - 1]) < 1.0E-6D);
        }
        check(BowRefinement.targetRange(5) == 2.0D * BowRefinement.targetRange(1));
        check(BowRefinement.targetRange(0) == 8.0D);
        check(BowRefinement.drawMovementBonus(0) == 0.0D);
        System.out.println("Bow refinement range and draw movement checks passed");
    }

    private static void check(boolean valid) {
        if (!valid) throw new AssertionError();
    }
}
