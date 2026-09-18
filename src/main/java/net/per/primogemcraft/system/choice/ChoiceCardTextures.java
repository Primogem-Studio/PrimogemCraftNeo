package net.per.primogemcraft.system.choice;

import net.minecraft.resources.ResourceLocation;

public record ChoiceCardTextures(ResourceLocation sheet, int columns, int tileWidth, int tileHeight, int idle,
                                 int hovered, int chosen, int dimmed, ResourceLocation backSheet) {
    public ChoiceCardTextures(ResourceLocation sheet, int columns, int tileWidth, int tileHeight, int idle, int hovered, int chosen, int dimmed, ResourceLocation backSheet) {
        this.sheet = sheet;
        this.columns = Math.max(1, columns);
        this.tileWidth = Math.max(1, tileWidth);
        this.tileHeight = Math.max(1, tileHeight);
        this.idle = idle;
        this.hovered = hovered;
        this.chosen = chosen;
        this.dimmed = dimmed;
        this.backSheet = backSheet;
    }

    public int u(int column) {
        return tileWidth * column;
    }

    public int v(int row) {
        return tileHeight * row;
    }
}
