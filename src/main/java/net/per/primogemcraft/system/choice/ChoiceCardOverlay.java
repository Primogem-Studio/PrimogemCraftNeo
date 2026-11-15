package net.per.primogemcraft.system.choice;

import net.minecraft.util.Identifier;

public record ChoiceCardOverlay(Identifier sheet, int columns, int column) {
    public ChoiceCardOverlay {
        columns = Math.max(1, columns);
        column = net.minecraft.util.math.MathHelper.clamp(column, 0, columns - 1);
    }

    public static ChoiceCardOverlay of(Identifier sheet, int columns, int column) {
        return new ChoiceCardOverlay(sheet, columns, column);
    }
}
