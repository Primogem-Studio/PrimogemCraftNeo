package net.per.primogemcraft.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.per.primogemcraft.system.menu.ContainerWindowLayout;
import net.per.primogemcraft.system.menu.ContainerWindowMenu;

public class ContainerWindowScreen extends AbstractContainerScreen<ContainerWindowMenu> {
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

    public ContainerWindowScreen(ContainerWindowMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        imageWidth = ContainerWindowLayout.panelWidth(menu.containerSlots());
        imageHeight = ContainerWindowLayout.panelHeight(menu.visibleRows());
        super.init();
        titleLabelX = ContainerWindowLayout.GRID_X;
        inventoryLabelX = ContainerWindowLayout.GRID_X;
        inventoryLabelY = ContainerWindowLayout.inventoryY(menu.visibleRows()) - INVENTORY_LABEL_OFFSET;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        advance(mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(leftPos, topPos, 0.0F);
        GuiAtlas.panel(graphics, 0, 0, imageWidth, imageHeight);
        for (var index = menu.containerSlots(); index < menu.slots.size(); index++) {
            var slot = menu.getSlot(index);
            slotCell(graphics, slot.x, slot.y, 0.0F);
        }
        renderGrid(graphics);
        pose.popPose();
    }

    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (slot.container != menu.container()) {
            super.renderSlot(graphics, slot);
            return;
        }
        var y = cellY(slot.index);
        if (y + SLOT_ITEM <= ContainerWindowLayout.gridTop()
                || y >= ContainerWindowLayout.gridTop() + ContainerWindowLayout.gridHeight(menu.visibleRows())) return;
        clipGrid(graphics);
        graphics.pose().pushPose();
        graphics.pose().translate(ContainerWindowLayout.columnX(slot.index % ContainerWindowLayout.COLUMNS) - slot.x,
                y - slot.y, 0.0F);
        super.renderSlot(graphics, slot);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    @Override
    protected void renderSlotHighlight(GuiGraphics graphics, Slot slot, int mouseX, int mouseY, float partialTick) {
        if (slot.container != menu.container()) super.renderSlotHighlight(graphics, slot, mouseX, mouseY, partialTick);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (y == ContainerWindowLayout.HIDDEN_Y && x <= ContainerWindowLayout.HIDDEN_X) {
            var index = ContainerWindowLayout.HIDDEN_X - x;
            return index < menu.containerSlots() && cellAt(mouseX, mouseY) == index;
        }
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0.0D || !gridContains(mouseX, mouseY)) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        scroll.scroll(scrollY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !isQuickCrafting && canScroll() && scrollbarContains(mouseX, mouseY)) {
            draggingScrollbar = true;
            dragScrollbar(mouseY);
            return true;
        }
        quickMoving = (button == 0 || button == 1) && hasShiftDown() && menu.getCarried().isEmpty();
        lastQuickMoveSlot = quickMoving ? slotAt(mouseX, mouseY) : null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            dragScrollbar(mouseY);
            return true;
        }
        if (quickMoving && hasShiftDown() && menu.getCarried().isEmpty()) {
            var slot = slotAt(mouseX, mouseY);
            if (slot != null && slot != lastQuickMoveSlot && slot.hasItem() && slot.mayPickup(minecraft.player)) {
                slotClicked(slot, slot.index, button, ClickType.QUICK_MOVE);
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

    private void renderGrid(GuiGraphics graphics) {
        var rows = menu.visibleRows();
        var firstRow = Mth.floor(scroll.shown());
        var top = cellY(firstRow * ContainerWindowLayout.COLUMNS);
        clipGrid(graphics);
        for (var row = 0; row <= rows; row++) {
            var y = top + row * ContainerWindowLayout.SLOT;
            for (var column = 0; column < ContainerWindowLayout.COLUMNS; column++) {
                var index = (firstRow + row) * ContainerWindowLayout.COLUMNS + column;
                if (index >= menu.containerSlots()) break;
                var x = ContainerWindowLayout.columnX(column);
                slotCell(graphics, x, y, index == hoveredCell ? cellHover : 0.0F);
            }
        }
        graphics.disableScissor();
        if (canScroll()) renderScrollbar(graphics);
    }

    private void renderScrollbar(GuiGraphics graphics) {
        var x = ContainerWindowLayout.scrollbarX();
        var top = ContainerWindowLayout.gridTop();
        var height = ContainerWindowLayout.gridHeight(menu.visibleRows());
        GuiAtlas.stretched(graphics, GuiAtlas.SCROLL_TRACK, x, top, ContainerWindowLayout.SCROLLBAR_WIDTH, height);
        var thumb = thumbSize();
        var offset = top + Math.round((height - thumb) * scroll.progress());
        GuiAtlas.stretched(graphics, GuiAtlas.SCROLL_THUMB, x, offset, ContainerWindowLayout.SCROLLBAR_WIDTH, thumb);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredCell >= 0) {
            var stack = menu.getSlot(hoveredCell).getItem();
            if (!stack.isEmpty()) {
                graphics.renderTooltip(font, getTooltipFromContainerItem(stack), stack.getTooltipImage(), stack, mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private void advance(int mouseX, int mouseY) {
        scroll.setLimit(Math.max(0, ContainerWindowLayout.rows(menu.containerSlots()) - menu.visibleRows()));
        scroll.advance(frameSeconds());
        hoveredCell = cellAt(mouseX, mouseY);
        var ease = 1.0F - (float) Math.exp(-HOVER_RESPONSE * frameSeconds());
        cellHover = Mth.lerp(ease, cellHover, hoveredCell >= 0 ? 1.0F : 0.0F);
    }

    private void slotCell(GuiGraphics graphics, int x, int y, float hover) {
        GuiAtlas.sprite(graphics, GuiAtlas.SLOT, x - 1, y - 1);
        if (hover <= HOVER_FLOOR) return;
        var alpha = Mth.clamp((int) (HIGHLIGHT_ALPHA * hover), 0, HIGHLIGHT_ALPHA);
        var highlight = alpha << 24 | HIGHLIGHT_RGB;
        graphics.fillGradient(x, y, x + SLOT_ITEM, y + SLOT_ITEM, highlight, highlight);
    }

    private int cellAt(double mouseX, double mouseY) {
        if (!gridContains(mouseX, mouseY)) return -1;
        var localX = mouseX - leftPos - ContainerWindowLayout.GRID_X + 1.0D;
        var column = (int) Math.floor(localX / ContainerWindowLayout.SLOT);
        var row = (int) Math.floor((mouseY - topPos - cellY(0) + 1.0D) / ContainerWindowLayout.SLOT);
        var index = row * ContainerWindowLayout.COLUMNS + column;
        return index < menu.containerSlots() ? index : -1;
    }

    private int cellY(int index) {
        return ContainerWindowLayout.GRID_Y + index / ContainerWindowLayout.COLUMNS * ContainerWindowLayout.SLOT
                - Math.round(scroll.shown() * ContainerWindowLayout.SLOT);
    }

    private void clipGrid(GuiGraphics graphics) {
        graphics.enableScissor(leftPos + ContainerWindowLayout.GRID_X - 1, topPos + ContainerWindowLayout.gridTop(),
                leftPos + ContainerWindowLayout.GRID_X - 1 + ContainerWindowLayout.gridWidth(),
                topPos + ContainerWindowLayout.gridTop() + ContainerWindowLayout.gridHeight(menu.visibleRows()));
    }

    private Slot slotAt(double mouseX, double mouseY) {
        for (var slot : menu.slots) {
            if (slot.isActive() && isHovering(slot.x, slot.y, SLOT_ITEM, SLOT_ITEM, mouseX, mouseY)) return slot;
        }
        return null;
    }

    private boolean gridContains(double mouseX, double mouseY) {
        var localX = mouseX - leftPos - ContainerWindowLayout.GRID_X + 1.0D;
        var localY = mouseY - topPos - ContainerWindowLayout.gridTop();
        return localX >= 0.0D && localX < ContainerWindowLayout.gridWidth()
                && localY >= 0.0D && localY < ContainerWindowLayout.gridHeight(menu.visibleRows());
    }

    private boolean scrollbarContains(double mouseX, double mouseY) {
        var localX = mouseX - leftPos - ContainerWindowLayout.scrollbarX();
        var localY = mouseY - topPos - ContainerWindowLayout.gridTop();
        return localX >= 0.0D && localX < ContainerWindowLayout.SCROLLBAR_WIDTH
                && localY >= 0.0D && localY < ContainerWindowLayout.gridHeight(menu.visibleRows());
    }

    private void dragScrollbar(double mouseY) {
        var height = ContainerWindowLayout.gridHeight(menu.visibleRows());
        var travel = height - thumbSize();
        if (travel <= 0) return;
        var local = mouseY - topPos - ContainerWindowLayout.gridTop() - thumbSize() / 2.0D;
        scroll.jumpTo((float) (local / travel));
    }

    private boolean canScroll() {
        return ContainerWindowLayout.scrollable(menu.containerSlots());
    }

    private int thumbSize() {
        var height = ContainerWindowLayout.gridHeight(menu.visibleRows());
        var rows = ContainerWindowLayout.rows(menu.containerSlots());
        return Math.max(GuiAtlas.SCROLL_THUMB.height(), Math.round(height * (menu.visibleRows() / (float) rows)));
    }

    private float frameSeconds() {
        if (minecraft == null) return 0.0F;
        return Mth.clamp(minecraft.getTimer().getGameTimeDeltaTicks(), 0.0F, 4.0F) * 0.05F;
    }
}
