package net.per.primogemcraft.system.choice;

import net.minecraft.resources.ResourceLocation;

public record ChoiceCardOverlay(ResourceLocation sheet, int columns, int column) {
    public ChoiceCardOverlay {
        columns = Math.max(1, columns);
        column = Math.clamp(column, 0, columns - 1);
    }

    public static ChoiceCardOverlay of(ResourceLocation sheet, int columns, int column) {
        return new ChoiceCardOverlay(sheet, columns, column);
    }
}
