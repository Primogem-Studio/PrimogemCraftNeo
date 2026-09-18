package net.per.primogemcraft.system.choice;

import net.minecraft.util.Mth;

public enum ChoiceSpinSpeed {
    SLOW(1260, 1800, 2520, 3240),
    MEDIUM(950, 1350, 1890, 2430),
    FAST(700, 1000, 1400, 1800),
    BRISK(500, 680, 880, 1120),
    FASTEST(470, 670, 940, 1210);

    private static final int MILLIS_PER_TICK = 50;

    private final int[] tierMillis;

    ChoiceSpinSpeed(int... tierMillis) {
        this.tierMillis = tierMillis;
    }

    public int durationMillis(int turns) {
        return tierMillis[Mth.clamp(turns, 1, tierMillis.length) - 1];
    }

    public int durationTicks(int turns) {
        return durationMillis(turns) / MILLIS_PER_TICK;
    }

    public String translationKey() {
        return "gui.primogemcraft.choice.speed." + name().toLowerCase();
    }
}
