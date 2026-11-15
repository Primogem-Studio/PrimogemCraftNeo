package net.per.primogemcraft.system.choice;

public record ChoiceCardSpin(int turns, ChoiceSpinSpeed speed) {
    private static final int MAX_TURNS = 24;

    public ChoiceCardSpin {
        turns = net.minecraft.util.math.MathHelper.clamp(turns, 1, MAX_TURNS);
    }

    public static ChoiceCardSpin of(int turns, ChoiceSpinSpeed speed) {
        return new ChoiceCardSpin(turns, speed);
    }

    public int ticks() {
        return speed.durationTicks(turns);
    }
}
