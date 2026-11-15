package net.per.primogemcraft.system.choice;

import net.minecraft.util.Identifier;

public record ChoiceCardIcons(Identifier texture, int count) {
    private static final int MAX_COUNT = 6;

    public ChoiceCardIcons {
        count = net.minecraft.util.math.MathHelper.clamp(count, 1, MAX_COUNT);
    }

    public static ChoiceCardIcons of(Identifier texture, int count) {
        return new ChoiceCardIcons(texture, count);
    }
}
