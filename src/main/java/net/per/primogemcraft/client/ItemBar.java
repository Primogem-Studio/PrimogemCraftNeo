package net.per.primogemcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

final class ItemBar {
    static final int LEFT = 2;
    static final int WIDTH = 13;
    static final int HEIGHT = 2;
    static final int DURABILITY_TOP = 13;
    static final int STACKS_TOP = 11;

    private static final int OPAQUE = 0xFF000000;

    private ItemBar() {
    }

    static void render(GuiGraphics graphics, int x, int y, int top, int filled, int color) {
        graphics.fill(RenderType.guiOverlay(), x + LEFT, y + top, x + LEFT + WIDTH, y + top + HEIGHT, OPAQUE);
        graphics.fill(RenderType.guiOverlay(), x + LEFT, y + top, x + LEFT + filled, y + top + 1, color | OPAQUE);
    }

    static int filled(int value, int capacity) {
        return capacity <= 0 ? 0 : (int) Math.min(WIDTH, (float) value / capacity * WIDTH);
    }
}
