package net.per.primogemcraft.system.weapon;

public record BowAttackCycle(int minimumCooldown, int maximumCooldown, int frameCount, int frameTicks) {
    public BowAttackCycle {
        if (minimumCooldown < 1 || maximumCooldown < minimumCooldown || maximumCooldown > 1200)
            throw new IllegalArgumentException("Invalid bow cooldown range");
        if (frameCount < 2 || frameCount > 256 || frameTicks < 1 || frameTicks > 1200)
            throw new IllegalArgumentException("Invalid bow animation timing");
    }

    public int cooldown(double roll) {
        if (!Double.isFinite(roll) || roll < 0.0D || roll >= 1.0D)
            throw new IllegalArgumentException("Cooldown roll must be in [0, 1)");
        return minimumCooldown + (int) (roll * (maximumCooldown - minimumCooldown + 1));
    }

    public int frame(long elapsedTicks) {
        if (elapsedTicks < 0 || elapsedTicks >= (long) (frameCount - 1) * frameTicks) return 0;
        return 1 + (int) (elapsedTicks / frameTicks);
    }
}
