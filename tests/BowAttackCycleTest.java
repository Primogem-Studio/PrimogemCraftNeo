import net.per.primogemcraft.system.weapon.BowAttackCycle;

public final class BowAttackCycleTest {
    public static void main(String[] args) {
        var cycle = new BowAttackCycle(8, 16, 5);
        check(cycle.cooldown(0.0D) == 8);
        check(cycle.cooldown(Math.nextDown(1.0D)) == 16);
        for (var duration = 8; duration <= 16; duration++) {
            for (var tick = -2; tick <= duration + 2; tick++) {
                var expected = tick < 0 ? 0 : Math.min(4, tick * 5 / duration);
                check(cycle.frame(tick, duration) == expected);
            }
            check(cycle.frame(0, duration) == 0);
            check(cycle.frame(duration - 1, duration) == 4);
        }
        check(cycle.frame(Long.MAX_VALUE, 8) == 4);
        check(cycle.frame(Long.MIN_VALUE, 8) == 0);
        check(cycle.frame(1, 0) == 0);
        check(new BowAttackCycle(10, 10, 2).cooldown(0.7D) == 10);
        for (var sample = 0; sample < 10000; sample++) {
            var cooldown = cycle.cooldown(sample / 10000.0D);
            check(cooldown >= 8 && cooldown <= 16);
        }
        rejects(() -> new BowAttackCycle(0, 8, 5));
        rejects(() -> new BowAttackCycle(8, 7, 5));
        rejects(() -> new BowAttackCycle(8, 16, 1));
        rejects(() -> new BowAttackCycle(8, 16, 257));
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
