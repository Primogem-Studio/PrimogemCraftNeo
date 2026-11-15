package net.per.primogemcraft.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.system.menu.ContainerWindowLayout;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;

public class ContainerWindowScreen extends HandledScreen<ContainerWindowMenu> {
    private static final float HOVER_FLOOR = 0.05F;
    private static final float HOVER_RESPONSE = 18.0F;
    private static final int INVENTORY_LABEL_OFFSET = 11;
    private static final int SLOT_ITEM = 16;
    private static final int HIGHLIGHT_ALPHA = 0x80;
    private static final int HIGHLIGHT_RGB = 0xFFFFFF;

    private final SmoothScroll scroll = new SmoothScroll();
    private boolean draggingScrollbar;
    private boolean quickMoving;
    private Slot lastQuickMoveSlot;
    private int hoveredCell = -1;
    private float cellHover;

    public ContainerWindowScreen(ContainerWindowMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        backgroundWidth = ContainerWindowLayout.panelWidth(handler.containerSlots());
        backgroundHeight = ContainerWindowLayout.panelHeight(handler.visibleRows());
        super.init();
        titleX = ContainerWindowLayout.GRID_X;
        playerInventoryTitleX = ContainerWindowLayout.GRID_X;
        playerInventoryTitleY = ContainerWindowLayout.inventoryY(handler.visibleRows()) - INVENTORY_LABEL_OFFSET;
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
        advance(mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        drawMouseoverTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTick, int mouseX, int mouseY) {
        var pose = graphics.getMatrices();
        pose.push();
        pose.translate(x, y, 0.0F);
        GuiAtlas.panel(graphics, 0, 0, backgroundWidth, backgroundHeight);
        for (var index = handler.containerSlots(); index < handler.slots.size(); index++) {
            var slot = handler.getSlot(index);
            slotCell(graphics, slot.x, slot.y, 0.0F);
        }
        renderGrid(graphics);
        pose.pop();

        // 容器格：原版 drawSlot 为 private，这里在裁剪区内自行绘制物品与数量
        var firstRow = MathHelper.floor(scroll.shown());
        var top = cellY(firstRow * ContainerWindowLayout.COLUMNS);
        clipGrid(graphics);
        for (var row = 0; row <= handler.visibleRows(); row++) {
            var cy = top + row * ContainerWindowLayout.SLOT;
            for (var column = 0; column < ContainerWindowLayout.COLUMNS; column++) {
                var index = (firstRow + row) * ContainerWindowLayout.COLUMNS + column;
                if (index >= handler.containerSlots()) break;
                var slot = handler.getSlot(index);
                var cx = ContainerWindowLayout.columnX(column);
                var stack = slot.getStack();
                if (!stack.isEmpty()) {
                    graphics.drawItem(stack, x + cx, y + cy);
                    graphics.drawItemInSlot(textRenderer, stack, x + cx, y + cy);
                }
                if (index == hoveredCell && handler.getCursorStack().isEmpty())
                    drawSlotHighlight(graphics, x + cx, y + cy, 0);
            }
        }
        graphics.disableScissor();
    }

    @Override
    protected boolean isPointWithinBounds(int sx, int sy, int width, int height, double mouseX, double mouseY) {
        if (sy == ContainerWindowLayout.HIDDEN_Y && sx <= ContainerWindowLayout.HIDDEN_X) {
            var index = ContainerWindowLayout.HIDDEN_X - sx;
            return index < handler.containerSlots() && cellAt(mouseX, mouseY) == index;
        }
        return super.isPointWithinBounds(sx, sy, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (scrollY == 0.0D || !gridContains(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, scrollY);
        scroll.scroll(scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && canScroll() && scrollbarContains(mouseX, mouseY)) {
            draggingScrollbar = true;
            dragScrollbar(mouseY);
            return true;
        }
        quickMoving = (button == 0 || button == 1) && hasShiftDown() && handler.getCursorStack().isEmpty();
        lastQuickMoveSlot = quickMoving ? slotAt(mouseX, mouseY) : null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            dragScrollbar(mouseY);
            return true;
        }
        if (quickMoving && hasShiftDown() && handler.getCursorStack().isEmpty()) {
            var slot = slotAt(mouseX, mouseY);
            if (slot != null && slot != lastQuickMoveSlot && slot.hasStack() && slot.canTakeItems(client.player)) {
                onMouseClick(slot, slot.id, button, SlotActionType.QUICK_MOVE);
            }
            lastQuickMoveSlot = slot;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        quickMoving = false;
        lastQuickMoveSlot = null;
        if (draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderGrid(DrawContext graphics) {
        var rows = handler.visibleRows();
        var firstRow = MathHelper.floor(scroll.shown());
        var top = cellY(firstRow * ContainerWindowLayout.COLUMNS);
        clipGrid(graphics);
        for (var row = 0; row <= rows; row++) {
            var cy = top + row * ContainerWindowLayout.SLOT;
            for (var column = 0; column < ContainerWindowLayout.COLUMNS; column++) {
                var index = (firstRow + row) * ContainerWindowLayout.COLUMNS + column;
                if (index >= handler.containerSlots()) break;
                var cx = ContainerWindowLayout.columnX(column);
                slotCell(graphics, cx, cy, index == hoveredCell ? cellHover : 0.0F);
            }
        }
        graphics.disableScissor();
        if (canScroll()) renderScrollbar(graphics);
    }

    private void renderScrollbar(DrawContext graphics) {
        var sx = ContainerWindowLayout.scrollbarX();
        var top = ContainerWindowLayout.gridTop();
        var height = ContainerWindowLayout.gridHeight(handler.visibleRows());
        GuiAtlas.stretched(graphics, GuiAtlas.SCROLL_TRACK, sx, top, ContainerWindowLayout.SCROLLBAR_WIDTH, height);
        var thumb = thumbSize();
        var offset = top + Math.round((height - thumb) * scroll.progress());
        GuiAtlas.stretched(graphics, GuiAtlas.SCROLL_THUMB, sx, offset, ContainerWindowLayout.SCROLLBAR_WIDTH, thumb);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext graphics, int mouseX, int mouseY) {
        if (handler.getCursorStack().isEmpty() && hoveredCell >= 0) {
            var stack = handler.getSlot(hoveredCell).getStack();
            if (!stack.isEmpty()) {
                graphics.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
                return;
            }
        }
        super.drawMouseoverTooltip(graphics, mouseX, mouseY);
    }

    private void advance(int mouseX, int mouseY) {
        scroll.setLimit(Math.max(0, ContainerWindowLayout.rows(handler.containerSlots()) - handler.visibleRows()));
        scroll.advance(frameSeconds());
        hoveredCell = cellAt(mouseX, mouseY);
        var ease = 1.0F - (float) Math.exp(-HOVER_RESPONSE * frameSeconds());
        cellHover = MathHelper.lerp(ease, cellHover, hoveredCell >= 0 ? 1.0F : 0.0F);
    }

    private void slotCell(DrawContext graphics, int sx, int sy, float hover) {
        GuiAtlas.sprite(graphics, GuiAtlas.SLOT, sx - 1, sy - 1);
        if (hover <= HOVER_FLOOR) return;
        var alpha = MathHelper.clamp((int) (HIGHLIGHT_ALPHA * hover), 0, HIGHLIGHT_ALPHA);
        var highlight = alpha << 24 | HIGHLIGHT_RGB;
        graphics.fillGradient(sx, sy, sx + SLOT_ITEM, sy + SLOT_ITEM, highlight, highlight);
    }

    private int cellAt(double mouseX, double mouseY) {
        if (!gridContains(mouseX, mouseY)) return -1;
        var localX = mouseX - x - ContainerWindowLayout.GRID_X + 1.0D;
        var column = (int) Math.floor(localX / ContainerWindowLayout.SLOT);
        var row = (int) Math.floor((mouseY - y - cellY(0) + 1.0D) / ContainerWindowLayout.SLOT);
        var index = row * ContainerWindowLayout.COLUMNS + column;
        return index < handler.containerSlots() ? index : -1;
    }

    private int cellY(int index) {
        return ContainerWindowLayout.GRID_Y + index / ContainerWindowLayout.COLUMNS * ContainerWindowLayout.SLOT
                - Math.round(scroll.shown() * ContainerWindowLayout.SLOT);
    }

    private void clipGrid(DrawContext graphics) {
        graphics.enableScissor(x + ContainerWindowLayout.GRID_X - 1, y + ContainerWindowLayout.gridTop(),
                x + ContainerWindowLayout.GRID_X - 1 + ContainerWindowLayout.gridWidth(),
                y + ContainerWindowLayout.gridTop() + ContainerWindowLayout.gridHeight(handler.visibleRows()));
    }

    private Slot slotAt(double mouseX, double mouseY) {
        for (var slot : handler.slots) {
            if (slot.isEnabled() && isPointWithinBounds(slot.x, slot.y, SLOT_ITEM, SLOT_ITEM, mouseX, mouseY)) return slot;
        }
        return null;
    }

    private boolean gridContains(double mouseX, double mouseY) {
        var localX = mouseX - x - ContainerWindowLayout.GRID_X + 1.0D;
        var localY = mouseY - y - ContainerWindowLayout.gridTop();
        return localX >= 0.0D && localX < ContainerWindowLayout.gridWidth()
                && localY >= 0.0D && localY < ContainerWindowLayout.gridHeight(handler.visibleRows());
    }

    private boolean scrollbarContains(double mouseX, double mouseY) {
        var localX = mouseX - x - ContainerWindowLayout.scrollbarX();
        var localY = mouseY - y - ContainerWindowLayout.gridTop();
        return localX >= 0.0D && localX < ContainerWindowLayout.SCROLLBAR_WIDTH
                && localY >= 0.0D && localY < ContainerWindowLayout.gridHeight(handler.visibleRows());
    }

    private void dragScrollbar(double mouseY) {
        var height = ContainerWindowLayout.gridHeight(handler.visibleRows());
        var travel = height - thumbSize();
        if (travel <= 0) return;
        var local = mouseY - y - ContainerWindowLayout.gridTop() - thumbSize() / 2.0D;
        scroll.jumpTo((float) (local / travel));
    }

    private boolean canScroll() {
        return ContainerWindowLayout.scrollable(handler.containerSlots());
    }

    private int thumbSize() {
        var height = ContainerWindowLayout.gridHeight(handler.visibleRows());
        var rows = ContainerWindowLayout.rows(handler.containerSlots());
        return Math.max(GuiAtlas.SCROLL_THUMB.height(), Math.round(height * (handler.visibleRows() / (float) rows)));
    }

    private float frameSeconds() {
        if (client == null) return 0.0F;
        return MathHelper.clamp(client.getLastFrameDuration(), 0.0F, 4.0F) * 0.05F;
    }
}