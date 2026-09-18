package net.per.primogemcraft.client.gui;

import net.minecraft.util.Mth;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;

public final class GorgeousSmithingTableLayout {
    public static final int GRID_CELL = 40;
    public static final int GRID_ITEM_SCALE = 2;
    public static final int SLOT_CELL = 20;
    public static final int SLOT_INSET = 2;
    public static final int SCROLLBAR_WIDTH = 4;
    public static final int BAR_HEIGHT = 8;

    private static final int MARGIN = 8;
    private static final int GAP = 8;
    private static final int PAD = 6;
    private static final int TITLE_HEIGHT = 18;
    private static final int ACTION_FRAME_PAD = 4;
    private static final int ACTION_FRAME_TOP_PAD = 3;
    private static final int PREVIEW_COLUMN_SHARE = 24;
    private static final int SHARE = 100;
    private static final int MIN_PREVIEW_WIDTH = 76;
    private static final int MAX_PREVIEW_WIDTH = 240;
    private static final int PREVIEW_MIN_HEIGHT = 64;
    private static final float PREVIEW_HEIGHT_SHARE = 0.62F;
    private static final float PREVIEW_RISE = 0.24F;
    private static final float BUTTON_HEIGHT_SHARE = 0.10F;
    private static final float LEVEL_HEIGHT_SHARE = 0.15F;
    private static final float SLOT_HEIGHT_SHARE = 0.10F;
    private static final int MIN_BUTTON_HEIGHT = 20;
    private static final int MAX_BUTTON_HEIGHT = 36;
    private static final int MIN_LEVEL_HEIGHT = 44;
    private static final int MAX_LEVEL_HEIGHT = 58;
    private static final int MIN_SLOT_HEIGHT = 28;
    private static final int MAX_SLOT_HEIGHT = 42;
    private static final int MIN_PANEL_HEIGHT = 40;
    private static final int FILL_BUTTON_SIZE = 26;
    private static final int PREVIEW_ITEM_UNIT = 32;
    private static final int MIN_ITEM_SCALE = 2;
    private static final int MAX_ITEM_SCALE = 6;
    private static final int LEVEL_TEXT_STEP = 12;
    private static final int GRID_RIGHT_INSET = 3;
    private static final int GRID_PAD = 3;
    private static final int SLOT_WELL_PAD = 4;

    public final Rect weaponPanel;
    public final Rect weaponWell;
    public final Rect weaponGrid;
    public final int weaponScrollX;
    public final int weaponColumns;
    public final int weaponRows;
    public final int weaponCell;

    public final Rect previewPanel;
    public final int previewItemScale;
    public final Rect hintArea;

    public final Rect materialPanel;
    public final Rect materialWell;
    public final Rect materialGrid;
    public final int materialScrollX;
    public final int materialColumns;
    public final int materialRows;
    public final int materialCell;

    public final Rect levelPanel;
    public final int levelTextY;
    public final int levelPreviewY;
    public final Rect levelBar;

    public final Rect slotPanel;
    public final Rect slotWell;
    public final int slotStartX;
    public final int slotY;

    public final Rect fillButton;
    public final Rect enhanceButton;
    public final Rect actionFrame;

    public GorgeousSmithingTableLayout(int width, int height) {
        var contentY = MARGIN;
        var contentHeight = Math.max(1, height - MARGIN * 2);
        var middleWidth = Mth.clamp(Math.round((float) (width * PREVIEW_COLUMN_SHARE) / SHARE), MIN_PREVIEW_WIDTH, MAX_PREVIEW_WIDTH);
        var sideWidth = Math.max(1, (width - MARGIN * 2 - middleWidth - GAP * 2) / 2);
        var middleX = width / 2 - middleWidth / 2;
        var leftX = middleX - GAP - sideWidth;
        var rightX = middleX + middleWidth + GAP;

        var gridPad = GRID_PAD * 2;
        var gridSpaceW = sideWidth - PAD * 2 - SCROLLBAR_WIDTH - GRID_RIGHT_INSET;
        var gridInnerW = Math.max(1, gridSpaceW - gridPad);
        var weaponSpaceH = contentHeight - TITLE_HEIGHT - PAD;
        var weaponInnerH = Math.max(1, weaponSpaceH - gridPad);
        var weaponCols = Math.max(1, Math.round(gridInnerW / (float) GRID_CELL));
        var weaponRowCount = Math.max(1, Math.round(weaponInnerH / (float) GRID_CELL));
        weaponCell = Math.min(GRID_CELL, Math.min(gridInnerW / weaponCols, weaponInnerH / weaponRowCount));
        weaponColumns = Math.max(1, gridInnerW / weaponCell);
        weaponRows = Math.max(1, weaponInnerH / weaponCell);
        var weaponWellW = weaponColumns * weaponCell + gridPad;
        var weaponWellH = weaponRows * weaponCell + gridPad;
        weaponPanel = new Rect(leftX, contentY, sideWidth, contentHeight);
        weaponWell = new Rect(leftX + PAD + (gridSpaceW - weaponWellW) / 2, contentY + TITLE_HEIGHT + (weaponSpaceH - weaponWellH) / 2, weaponWellW, weaponWellH);
        weaponGrid = new Rect(weaponWell.x() + GRID_PAD, weaponWell.y() + GRID_PAD, weaponColumns * weaponCell, weaponRows * weaponCell);
        weaponScrollX = leftX + sideWidth - PAD - SCROLLBAR_WIDTH;

        var previewHeight = Math.max(PREVIEW_MIN_HEIGHT, Math.round(contentHeight * PREVIEW_HEIGHT_SHARE));
        var previewY = contentY + Math.round((contentHeight - previewHeight) * PREVIEW_RISE);
        previewPanel = new Rect(middleX, previewY, middleWidth, previewHeight);
        previewItemScale = Mth.clamp(middleWidth / PREVIEW_ITEM_UNIT, MIN_ITEM_SCALE, MAX_ITEM_SCALE);
        var hintY = previewY + previewHeight + GAP;
        hintArea = new Rect(middleX, hintY, middleWidth, Math.max(0, contentY + contentHeight - hintY));

        var gapAboveLevel = GAP - 1;
        var gapBelowLevel = GAP + 1;
        var buttonHeight = Mth.clamp(Math.round(contentHeight * BUTTON_HEIGHT_SHARE), MIN_BUTTON_HEIGHT, MAX_BUTTON_HEIGHT);
        var levelHeight = Mth.clamp(Math.round(contentHeight * LEVEL_HEIGHT_SHARE), MIN_LEVEL_HEIGHT, MAX_LEVEL_HEIGHT);
        var slotHeight = Mth.clamp(Math.round(contentHeight * SLOT_HEIGHT_SHARE), MIN_SLOT_HEIGHT, MAX_SLOT_HEIGHT);
        var fixed = gapAboveLevel + levelHeight + gapBelowLevel + slotHeight + GAP + buttonHeight + ACTION_FRAME_PAD;
        var materialHeight = Math.max(MIN_PANEL_HEIGHT, contentHeight - fixed);

        var materialSpaceH = materialHeight - TITLE_HEIGHT - PAD;
        var materialInnerH = Math.max(1, materialSpaceH - gridPad);
        var materialCols = Math.max(1, Math.round(gridInnerW / (float) GRID_CELL));
        var materialRowCount = Math.max(1, Math.round(materialInnerH / (float) GRID_CELL));
        materialCell = Math.min(GRID_CELL, Math.min(gridInnerW / materialCols, materialInnerH / materialRowCount));
        materialColumns = Math.max(1, gridInnerW / materialCell);
        materialRows = Math.max(1, materialInnerH / materialCell);
        var materialWellW = materialColumns * materialCell + gridPad;
        var materialWellH = materialRows * materialCell + gridPad;
        materialPanel = new Rect(rightX, contentY, sideWidth, materialHeight);
        materialWell = new Rect(rightX + PAD + (gridSpaceW - materialWellW) / 2, contentY + TITLE_HEIGHT + (materialSpaceH - materialWellH) / 2, materialWellW, materialWellH);
        materialGrid = new Rect(materialWell.x() + GRID_PAD, materialWell.y() + GRID_PAD, materialColumns * materialCell, materialRows * materialCell);
        materialScrollX = rightX + sideWidth - PAD - SCROLLBAR_WIDTH;

        levelPanel = new Rect(rightX, materialPanel.bottom() + gapAboveLevel, sideWidth, levelHeight);
        levelTextY = levelPanel.y() + PAD - 1;
        levelPreviewY = levelTextY + LEVEL_TEXT_STEP;
        levelBar = new Rect(rightX + PAD, levelPanel.y() + levelHeight - PAD - BAR_HEIGHT, sideWidth - PAD * 2, BAR_HEIGHT);

        slotPanel = new Rect(rightX, levelPanel.bottom() + gapBelowLevel, sideWidth, slotHeight);
        slotY = slotPanel.y() + (slotHeight - SLOT_CELL) / 2;
        var groupWidth = GorgeousSmithingTableMenu.MATERIAL_SLOT_COUNT * SLOT_CELL;
        slotStartX = slotPanel.x() + (sideWidth - groupWidth - GAP - FILL_BUTTON_SIZE) / 2;
        slotWell = new Rect(slotStartX - SLOT_WELL_PAD, slotY - SLOT_WELL_PAD, groupWidth + SLOT_WELL_PAD * 2, SLOT_CELL + SLOT_WELL_PAD * 2);
        fillButton = new Rect(slotStartX + groupWidth + GAP, slotPanel.y() + (slotHeight - FILL_BUTTON_SIZE) / 2, FILL_BUTTON_SIZE, FILL_BUTTON_SIZE);

        enhanceButton = new Rect(rightX + PAD, slotPanel.bottom() + GAP, sideWidth - PAD * 2, buttonHeight);
        actionFrame = new Rect(rightX, slotPanel.y() - ACTION_FRAME_TOP_PAD, sideWidth, enhanceButton.bottom() - slotPanel.y() + ACTION_FRAME_TOP_PAD + ACTION_FRAME_PAD);
    }

    public Rect materialSlot(int index) {
        return new Rect(slotStartX + index * SLOT_CELL, slotY, SLOT_CELL, SLOT_CELL);
    }

    public record Rect(int x, int y, int width, int height) {
        public int right() {
            return x + width;
        }

        public int bottom() {
            return y + height;
        }

        public int centerX() {
            return x + width / 2;
        }

        public int centerY() {
            return y + height / 2;
        }

        public boolean contains(double pointerX, double pointerY) {
            return pointerX >= x && pointerX < x + width && pointerY >= y && pointerY < y + height;
        }
    }
}
