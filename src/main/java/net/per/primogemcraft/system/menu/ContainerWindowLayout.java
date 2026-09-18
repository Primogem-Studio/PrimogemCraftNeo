package net.per.primogemcraft.system.menu;

/**
 * Geometry of the shared container window. Slot coordinates are the position of the 16 pixel item inside a slot, the
 * same convention {@code AbstractContainerScreen} uses, so a slot sprite drawn at that position minus one pixel covers
 * the cell exactly.
 */
public final class ContainerWindowLayout {
    public static final int SLOT = 18;
    public static final int COLUMNS = 9;
    public static final int MAX_VISIBLE_ROWS = 6;
    public static final int GRID_X = 8;
    public static final int GRID_Y = 18;
    public static final int SCROLLBAR_WIDTH = 6;
    public static final int SCROLLBAR_GAP = 6;
    public static final int PANEL_PADDING = 7;
    public static final int HIDDEN_X = -10000;
    public static final int HIDDEN_Y = -10000;

    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_GAP = 14;

    private ContainerWindowLayout() {
    }

    public static int rows(int slots) {
        return Math.max(1, (slots + COLUMNS - 1) / COLUMNS);
    }

    public static int visibleRows(int slots) {
        return Math.min(MAX_VISIBLE_ROWS, rows(slots));
    }

    public static boolean scrollable(int slots) {
        return rows(slots) > visibleRows(slots);
    }

    public static int columnX(int column) {
        return GRID_X + column * SLOT;
    }

    public static int gridWidth() {
        return COLUMNS * SLOT;
    }

    public static int gridTop() {
        return GRID_Y - 1;
    }

    public static int gridHeight(int visibleRows) {
        return visibleRows * SLOT;
    }

    public static int scrollbarX() {
        return GRID_X - 1 + gridWidth() + SCROLLBAR_GAP;
    }

    public static int inventoryY(int visibleRows) {
        return GRID_Y + visibleRows * SLOT + INVENTORY_GAP;
    }

    public static int hotbarY(int visibleRows) {
        return inventoryY(visibleRows) + INVENTORY_ROWS * SLOT + 4;
    }

    public static int panelWidth(int slots) {
        return contentRight(scrollable(slots)) + PANEL_PADDING;
    }

    public static int panelHeight(int visibleRows) {
        return hotbarY(visibleRows) + SLOT - 1 + PANEL_PADDING;
    }

    private static int contentRight(boolean scrollable) {
        return scrollable ? scrollbarX() + SCROLLBAR_WIDTH : GRID_X - 1 + gridWidth();
    }
}
