import net.per.primogemcraft.system.weapon.BowAttackCycle;

public final class BowAttackCycleTest {
    public static void main(String[] args) {
        var cycle = new BowAttackCycle(8, 16, 5, 2);
        check(cycle.cooldown(0.0D) == 8);
        check(cycle.cooldown(Math.nextDown(1.0D)) == 16);
        for (var tick = -2; tick < 20; tick++) {
            var expected = tick < 0 || tick >= 8 ? 0 : 1 + tick / 2;
            check(cycle.frame(tick) == expected);
        }
        check(cycle.frame(Long.MAX_VALUE) == 0);
        check(cycle.frame(Long.MIN_VALUE) == 0);
        check(new BowAttackCycle(10, 10, 2, 1).cooldown(0.7D) == 10);
        for (var sample = 0; sample < 10000; sample++) {
            var cooldown = cycle.cooldown(sample / 10000.0D);
            check(cooldown >= 8 && cooldown <= 16);
        }
        rejects(() -> new BowAttackCycle(0, 8, 5, 2));
        rejects(() -> new BowAttackCycle(8, 7, 5, 2));
        rejects(() -> new BowAttackCycle(8, 16, 1, 2));
        rejects(() -> new BowAttackCycle(8, 16, 5, 0));
        rejects(() -> cycle.cooldown(1.0D));
        rejects(() -> cycle.cooldown(-0.1D));
        rejects(() -> cycle.cooldown(Double.NaN));
        System.out.println("Bow attack cycle checks passed");
    }

    private static void check(boolean valid) {
        if (!valid) throw new AssertionError();
    }

    private static void rejects(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Invalid bow settings accepted");
    }
}
