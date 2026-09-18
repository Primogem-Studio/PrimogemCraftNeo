package net.per.primogemcraft.system.choice;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public record ChoiceSpin(int turns, ChoiceSpinSpeed speed, float tilt) {
    private static final int COMMON_TURNS = 1;
    private static final int UNCOMMON_TURNS = 2;
    private static final int RARE_TURNS = 3;
    private static final int EPIC_TURNS = 4;
    private static final int MAX_TURNS = 24;
    private static final float DEFAULT_TILT = 14.0F;
    private static final float MAX_TILT = 45.0F;

    public ChoiceSpin {
        turns = turns <= 0 ? 0 : Math.clamp(turns, 1, MAX_TURNS);
        tilt = Mth.clamp(tilt, 0.0F, MAX_TILT);
    }

    public static ChoiceSpin of(ChoiceSpinSpeed speed) {
        return new ChoiceSpin(0, speed, DEFAULT_TILT);
    }

    public static ChoiceSpin of(int turns, ChoiceSpinSpeed speed) {
        return new ChoiceSpin(turns, speed, DEFAULT_TILT);
    }

    public static ChoiceSpin of(int turns, ChoiceSpinSpeed speed, float tilt) {
        return new ChoiceSpin(turns, speed, tilt);
    }

    public ChoiceCardSpin cardSpin(ItemStack stack) {
        return ChoiceCardSpin.of(turnsFor(stack), speed);
    }

    private int turnsFor(ItemStack stack) {
        if (turns > 0) return turns;
        return switch (stack.getRarity()) {
            case COMMON -> COMMON_TURNS;
            case UNCOMMON -> UNCOMMON_TURNS;
            case RARE -> RARE_TURNS;
            case EPIC -> EPIC_TURNS;
        };
    }
}
