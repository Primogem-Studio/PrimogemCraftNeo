package net.per.primogemcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.client.StarRow;
import net.per.primogemcraft.client.gui.GorgeousSmithingTableLayout.Rect;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.menu.GorgeousSmithingTableMenu;
import net.per.primogemcraft.system.weapon.*;
import net.per.primogemcraft.system.wish.WishReports;

import java.util.*;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class GorgeousSmithingTableScreen extends HandledScreen<GorgeousSmithingTableMenu> {
    private static final int ITEM_SCALE = GorgeousSmithingTableLayout.GRID_ITEM_SCALE;
    private static final int SLOT_CELL = GorgeousSmithingTableLayout.SLOT_CELL;
    private static final int SLOT_INSET = GorgeousSmithingTableLayout.SLOT_INSET;
    private static final int SCROLLBAR_WIDTH = GorgeousSmithingTableLayout.SCROLLBAR_WIDTH;
    private static final int SCROLLBAR_MIN_THUMB = 12;
    private static final int TITLE_OFFSET = 6;
    private static final int TEXT_INSET = 6;
    private static final int HINT_MAX_LINES = 4;
    private static final int STAR_GAP = 3;
    private static final int BASE_STARS = 5;
    private static final int MAX_STARS = 8;
    private static final float HOVER_RESPONSE = 18.0F;
    private static final float LEVEL_RESPONSE = 12.0F;
    private static final float LEVEL_UP_TRAVEL_SECONDS = 0.45F;
    private static final float LEVEL_UP_HOLD_SECONDS = 0.15F;
    private static final float LEVEL_UP_NEXT_SECONDS = LEVEL_UP_TRAVEL_SECONDS + LEVEL_UP_HOLD_SECONDS;
    private static final float LEVEL_UP_END_SECONDS = LEVEL_UP_NEXT_SECONDS + LEVEL_UP_TRAVEL_SECONDS;
    private static final float PULSE_SPEED = 5.0F;
    private static final float CONFIRM_LOCK_SECONDS = 2.0F;
    private static final float MAX_TAG_SCALE = 0.5F;
    private static final int MAX_TAG_INSET = 2;
    private static final float PULSE_MIN = 0.35F;
    private static final float HIGHLIGHT_FLOOR = 0.05F;
    private static final float ARMED_TINT_RED = 1.0F;
    private static final float ARMED_TINT_GREEN = 0.42F;
    private static final float ARMED_TINT_BLUE = 0.42F;
    private static final String ELLIPSIS = "...";

    private static final String LABEL_PREFIX = "gui.primogemcraft.gorgeous_smithing_table.";
    private static final String WEAPONS_KEY = LABEL_PREFIX + "weapons";
    private static final String MATERIALS_KEY = LABEL_PREFIX + "materials";
    private static final String ENHANCE_KEY = LABEL_PREFIX + "enhance";
    private static final String ENHANCE_TOOLTIP_KEY = LABEL_PREFIX + "enhance_tooltip";
    private static final String CONFIRM_AGAIN_KEY = LABEL_PREFIX + "confirm_again";
    private static final List<String> CONFIRM_MESSAGE_KEYS = List.of(
            LABEL_PREFIX + "confirm_message.0",
            LABEL_PREFIX + "confirm_message.1",
            LABEL_PREFIX + "confirm_message.2");
    private static final String FILL_KEY = LABEL_PREFIX + "fill";
    private static final String FILL_TOOLTIP_KEY = LABEL_PREFIX + "fill_tooltip";
    private static final String REMAINING_KEY = LABEL_PREFIX + "remaining";
    private static final String LEVEL_PREVIEW_KEY = LABEL_PREFIX + "level_preview";
    private static final String REFINEMENT_LABEL_KEY = LABEL_PREFIX + "refinement_label";
    private static final String ATTACK_PREVIEW_KEY = LABEL_PREFIX + "attack_preview";
    private static final String NO_WEAPON_KEY = LABEL_PREFIX + "no_weapon";
    private static final String NO_MATERIALS_KEY = LABEL_PREFIX + "no_materials";
    private static final String EMPTY_WEAPONS_KEY = LABEL_PREFIX + "empty_weapons";
    private static final String SELECT_HINT_KEY = LABEL_PREFIX + "hint_select";
    private static final String LINKED_HINT_KEY = LABEL_PREFIX + "hint_linked";
    private static final String LINKED_SOURCE_KEY = LABEL_PREFIX + "linked_source";
    private static final String MAX_TAG_KEY = LABEL_PREFIX + "max_tag";
    private static final String LEVEL_KEY = "weapon.primogemcraft.level";
    private static final String MAX_LEVEL_KEY = "weapon.primogemcraft.level.max";

    private static final int FRAME_SIZE = 40;
    private static final int PANEL_INSET = 5;
    private static final int WELL_INSET = 4;
    private static final int SLICE_Y = FRAME_SIZE;
    private static final int SLICE_SIZE = PANEL_INSET * 2 + 1;
    private static final int ATLAS_WIDTH = FRAME_SIZE * 3;
    private static final int ATLAS_HEIGHT = SLICE_Y + SLICE_SIZE;

    private static final Identifier ATLAS = new Identifier(MOD_ID, "textures/gui/gorgeous_smithing_table.png");
    private static final Sprite FRAME = new Sprite(0, 0, FRAME_SIZE);
    private static final Sprite FRAME_HOVER = new Sprite(FRAME_SIZE, 0, FRAME_SIZE);
    private static final Sprite FRAME_CHOSEN = new Sprite(FRAME_SIZE * 2, 0, FRAME_SIZE);
    private static final Sprite PANEL = new Sprite(0, SLICE_Y, SLICE_SIZE);
    private static final Sprite WELL = new Sprite(SLICE_SIZE, SLICE_Y, WELL_INSET * 2 + 1);

    private static final int TITLE_COLOR = 0xFFEAF0F7;
    private static final int TEXT_COLOR = 0xFFDCE3EC;
    private static final int MUTED_COLOR = 0xFF98A3B1;
    private static final int WHITE_TEXT = 0xFFFFFFFF;
    private static final int MAX_TAG_LEVEL_COLOR = 0xFF55AAFF;
    private static final int MAX_TAG_REFINEMENT_COLOR = 0xFFFFD54F;
    private static final int MAX_TAG_FULL_COLOR = 0xFFFF446A;
    private static final int BAR_EDGE = 0x50FFFFFF;
    private static final int BAR_TRACK = 0xA0000000;
    private static final int BAR_FILL = 0xFF8FC181;
    private static final int BAR_PREVIEW = 0xFFE8FFDD;
    private static final int SCROLL_TRACK = 0x40000000;
    private static final int SCROLL_THUMB = 0xFFFFFFFF;
    private static final int SCRIM_TEXT = 0xFFB9C3CF;
    private static final int LINK_TINT = 0x1A8FD2FF;

    private final List<ItemStack> weaponRows = new ArrayList<>();
    private final List<Integer> weaponIndices = new ArrayList<>();
    private final List<MaterialRow> materialRows = new ArrayList<>();
    private final List<ItemStack> previewMaterials = new ArrayList<>();
    private final List<RowKey> materialOrder = new ArrayList<>();
    private final SmoothScroll weaponScroll = new SmoothScroll();
    private final SmoothScroll materialScroll = new SmoothScroll();

    private GorgeousSmithingTableLayout layout;
    private WeaponEnhancement.Preview preview;
    private int carriedOrigin = GorgeousSmithingTableMenu.NO_SELECTION;
    private int hoveredWeaponCell = -1;
    private int hoveredMaterialCell = -1;
    private int hoveredMaterialSlot = -1;
    private boolean buttonPressed;
    private boolean armed;
    private float armedSeconds;
    private boolean wasCarrying;
    private float experienceFill;
    private boolean levelUpActive;
    private boolean levelUpReal;
    private float levelUpSeconds;
    private float levelUpOrigin;
    private float levelUpResult;
    private int levelUpLevel;
    private int levelUpXp;
    private int observedSelection = GorgeousSmithingTableMenu.NO_SELECTION;
    private int observedLevel = -1;
    private int observedXp;
    private float pulse;
    private float weaponHover;
    private float materialHover;
    private float slotHover;
    private float buttonHover;
    private float fillHover;

    public GorgeousSmithingTableScreen(GorgeousSmithingTableMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        backgroundWidth = width;
        backgroundHeight = height;
        x = 0;
        y = 0;
        layout = new GorgeousSmithingTableLayout(width, height);
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
        advance(mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        focusedSlot = slotAt(mouseX, mouseY);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(DrawContext graphics) {
        // 面板由 drawBackground 在正常渲染流程中绘制，这里不画原版模糊背景。
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTick, int mouseX, int mouseY) {
        plate(graphics, layout.weaponPanel);
        plate(graphics, layout.previewPanel);
        plate(graphics, layout.materialPanel);
        plate(graphics, layout.levelPanel);
        plate(graphics, layout.actionFrame);
        well(graphics, layout.slotWell);

        graphics.drawText(textRenderer, Text.translatable(WEAPONS_KEY), layout.weaponPanel.x() + TITLE_OFFSET, layout.weaponPanel.y() + 5, TITLE_COLOR, false);
        graphics.drawText(textRenderer, Text.translatable(MATERIALS_KEY), layout.materialPanel.x() + TITLE_OFFSET, layout.materialPanel.y() + 5, TITLE_COLOR, false);

        renderWeaponGrid(graphics);
        renderMaterialGrid(graphics);
        renderPreview(graphics);
        renderLevel(graphics);
        renderButtons(graphics);
        renderHints(graphics);

        // 材料槽在菜单里被移到屏幕外，物品必须在这里自绘
        for (var index = 0; index < GorgeousSmithingTableMenu.FIRST_INVENTORY_SLOT; index++)
            renderContainerSlot(graphics, handler.getSlot(index));
    }

    /** 原版 renderSlot 为 private，改由 drawBackground 调用。 */
    private void renderContainerSlot(DrawContext graphics, Slot slot) {
        if (slot.id >= GorgeousSmithingTableMenu.FIRST_INVENTORY_SLOT) return;
        var rect = layout.materialSlot(slot.id);
        slotCell(graphics, rect.x(), rect.y(), SLOT_CELL, SLOT_CELL, FRAME_HOVER, hoveredMaterialSlot == slot.id ? slotHover : 0.0F);
        var stack = slot.getStack();
        if (stack.isEmpty()) return;
        graphics.drawItem(stack, rect.x() + SLOT_INSET, rect.y() + SLOT_INSET);
        graphics.drawItemInSlot(textRenderer, stack, rect.x() + SLOT_INSET, rect.y() + SLOT_INSET);
    }

    @Override
    protected void drawForeground(DrawContext graphics, int mouseX, int mouseY) {
    }

    /** 提示框统一由 renderHoverTooltip 负责，避免与父类重复绘制。 */
    @Override
    protected void drawMouseoverTooltip(DrawContext graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (scrollY == 0.0D) return false;
        if (layout.weaponPanel.contains(mouseX, mouseY)) {
            weaponScroll.scroll(scrollY);
            return true;
        }
        if (layout.materialPanel.contains(mouseX, mouseY)) {
            materialScroll.scroll(scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && layout.enhanceButton.contains(mouseX, mouseY) && handler.canEnhance()) {
            if (handler.hasInheritedMaterial() && !armed) {
                armed = true;
                armedSeconds = 0.0F;
                playSound(SoundEvents.UI_BUTTON_CLICK.value());
                return true;
            }
            if (armed && armedSeconds < CONFIRM_LOCK_SECONDS) return true;
            armed = false;
            return pressEnhance();
        }
        if (button == 0 && handler.canFill() && layout.fillButton.contains(mouseX, mouseY)) {
            playSound(SoundEvents.UI_BUTTON_CLICK.value());
            click(GorgeousSmithingTableMenu.FILL_BUTTON);
            return true;
        }
        var weaponCell = weaponCellAt(mouseX, mouseY);
        if (weaponCell >= 0 && button == 0) {
            carriedOrigin = GorgeousSmithingTableMenu.NO_SELECTION;
            playSound(SoundEvents.UI_BUTTON_CLICK.value());
            click(GorgeousSmithingTableMenu.SELECT_BUTTON_BASE + weaponIndices.get(weaponCell));
            return true;
        }
        var materialCell = materialCellAt(mouseX, mouseY);
        if (materialCell >= 0 && (button == 0 || button == 1)) {
            var row = materialRows.get(materialCell);
            if (row.linked()) {
                playSound(SoundEvents.UI_BUTTON_CLICK.value());
                click(GorgeousSmithingTableMenu.LINK_BUTTON_BASE + row.index());
                return true;
            }
            var shift = hasShiftDown();
            var mode = shift ? GorgeousSmithingTableMenu.BLUE_QUICK_MOVE
                    : button == 0 ? GorgeousSmithingTableMenu.BLUE_PICKUP : GorgeousSmithingTableMenu.BLUE_PICKUP_HALF;
            if (!shift && button == 0 && handler.getCursorStack().isEmpty() && !row.stack().isEmpty()) carriedOrigin = row.index();
            click(GorgeousSmithingTableMenu.BLUE_BUTTON_BASE + mode * GorgeousSmithingTableMenu.BLUE_BUTTON_STRIDE + row.index());
            return true;
        }
        var slot = slotAt(mouseX, mouseY);
        if (slot != null) {
            onMouseClick(slot, slot.id, button, hasShiftDown() ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        buttonPressed = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean pressEnhance() {
        buttonPressed = true;
        playSound(PGCSounds.ENHANCEMENT_SUCCESS.get());
        click(GorgeousSmithingTableMenu.ENHANCE_BUTTON);
        return true;
    }

    private void advance(int mouseX, int mouseY) {
        if (layout == null) return;
        var carrying = !handler.getCursorStack().isEmpty();
        if (wasCarrying && !carrying) {
            carriedOrigin = GorgeousSmithingTableMenu.NO_SELECTION;
            wasCarrying = false;
        } else {
            wasCarrying = carrying;
        }
        if (!handler.canEnhance() || !handler.hasInheritedMaterial()) armed = false;
        var seconds = frameSeconds();
        armedSeconds = armed ? armedSeconds + seconds : 0.0F;
        refreshLists();
        refreshPreview();
        var ease = 1.0F - (float) Math.exp(-HOVER_RESPONSE * seconds);
        pulse += seconds * PULSE_SPEED;
        if (pulse > MathHelper.TAU) pulse -= MathHelper.TAU;
        weaponScroll.setLimit(Math.max(0, weaponRowCount() - layout.weaponRows));
        materialScroll.setLimit(Math.max(0, materialRowCount() - layout.materialRows));
        weaponScroll.advance(seconds);
        materialScroll.advance(seconds);
        hoveredWeaponCell = weaponCellAt(mouseX, mouseY);
        hoveredMaterialCell = materialCellAt(mouseX, mouseY);
        hoveredMaterialSlot = slotIndexAt(mouseX, mouseY);
        weaponHover = MathHelper.lerp(ease, weaponHover, hoveredWeaponCell >= 0 ? 1.0F : 0.0F);
        materialHover = MathHelper.lerp(ease, materialHover, hoveredMaterialCell >= 0 ? 1.0F : 0.0F);
        slotHover = MathHelper.lerp(ease, slotHover, hoveredMaterialSlot >= 0 ? 1.0F : 0.0F);
        buttonHover = MathHelper.lerp(ease, buttonHover, handler.canEnhance() && layout.enhanceButton.contains(mouseX, mouseY) ? 1.0F : 0.0F);
        fillHover = MathHelper.lerp(ease, fillHover, handler.canFill() && layout.fillButton.contains(mouseX, mouseY) ? 1.0F : 0.0F);
    }

    private void refreshLists() {
        weaponRows.clear();
        weaponIndices.clear();
        materialRows.clear();
        if (client == null || client.player == null) return;
        var inventory = client.player.getInventory();
        var origin = dropTarget(inventory);
        var selected = handler.selectedIndex();
        var weapon = handler.selectedWeapon();
        for (var index = 0; index < GorgeousSmithingTableMenu.INVENTORY_SLOT_COUNT; index++) {
            var stack = inventory.getStack(index);
            if (stack.isEmpty()) {
                if (index != origin) continue;
                materialRows.add(new MaterialRow(ItemStack.EMPTY, index, false));
                continue;
            }
            if (stack.getItem() instanceof WishWeapon) {
                weaponRows.add(stack);
                weaponIndices.add(index);
                if (index == selected || index == origin || weapon.isEmpty() || stack.getItem() != weapon.getItem()) continue;
                materialRows.add(new MaterialRow(stack, index, false));
                continue;
            }
            if (index == selected || !WeaponEnhancement.isEnhancementMaterial(stack, weapon)) continue;
            materialRows.add(new MaterialRow(stack, index, false));
        }
        for (var ordinal = 0; ordinal < handler.linkedSlotCount(); ordinal++) {
            var stack = handler.getSlot(GorgeousSmithingTableMenu.FIRST_LINKED_SLOT + ordinal).getStack();
            if (stack.isEmpty() || !WeaponEnhancement.isEnhancementMaterial(stack, weapon)) continue;
            materialRows.add(new MaterialRow(stack, ordinal, true));
        }
        sortRows(weaponRows, weaponIndices, this::compareWeapons);
        sortMaterialRows();
    }

    private void sortMaterialRows() {
        if (!holdingCarried() || materialOrder.isEmpty()) {
            materialRows.sort(this::compareMaterials);
            materialOrder.clear();
            for (var row : materialRows) materialOrder.add(rowKey(row));
            return;
        }
        var positions = new HashMap<RowKey, Integer>();
        for (var position = 0; position < materialOrder.size(); position++) positions.putIfAbsent(materialOrder.get(position), position);
        materialRows.sort(Comparator.comparingInt(row -> positions.getOrDefault(rowKey(row), Integer.MAX_VALUE)));
    }

    private boolean holdingCarried() {
        return carriedOrigin >= 0 || !handler.getCursorStack().isEmpty();
    }

    private RowKey rowKey(MaterialRow row) {
        return new RowKey(row.linked(), row.index());
    }

    private int compareMaterials(MaterialRow first, MaterialRow second) {
        var level = Integer.compare(materialLevel(first.stack()), materialLevel(second.stack()));
        if (level != 0) return level;
        var refinement = Integer.compare(materialRefinement(first.stack()), materialRefinement(second.stack()));
        if (refinement != 0) return refinement;
        return Integer.compare(WeaponEnhancement.materialRank(first.stack()), WeaponEnhancement.materialRank(second.stack()));
    }

    private int materialLevel(ItemStack stack) {
        return stack.getItem() instanceof WishWeapon ? WeaponState.of(stack).level() : 0;
    }

    private int materialRefinement(ItemStack stack) {
        return stack.getItem() instanceof WishWeapon ? WeaponState.of(stack).refinements() : 0;
    }

    private void sortRows(List<ItemStack> rows, List<Integer> indices, Comparator<ItemStack> comparator) {
        var order = new ArrayList<Integer>(rows.size());
        for (var position = 0; position < rows.size(); position++) order.add(position);
        order.sort((first, second) -> comparator.compare(rows.get(first), rows.get(second)));
        var sortedRows = order.stream().map(rows::get).toList();
        var sortedIndices = order.stream().map(indices::get).toList();
        rows.clear();
        rows.addAll(sortedRows);
        indices.clear();
        indices.addAll(sortedIndices);
    }

    private int compareWeapons(ItemStack first, ItemStack second) {
        var level = Integer.compare(WeaponState.of(second).level(), WeaponState.of(first).level());
        if (level != 0) return level;
        return Integer.compare(materialRefinement(second), materialRefinement(first));
    }

    private int dropTarget(PlayerInventory inventory) {
        if (handler.getCursorStack().isEmpty()) return GorgeousSmithingTableMenu.NO_SELECTION;
        if (carriedOrigin >= 0 && carriedOrigin < GorgeousSmithingTableMenu.INVENTORY_SLOT_COUNT
                && inventory.getStack(carriedOrigin).isEmpty()) return carriedOrigin;
        for (var index = 0; index < GorgeousSmithingTableMenu.INVENTORY_SLOT_COUNT; index++)
            if (inventory.getStack(index).isEmpty()) return index;
        return GorgeousSmithingTableMenu.NO_SELECTION;
    }

    private void refreshPreview() {
        preview = null;
        previewMaterials.clear();
        if (client == null || client.player == null) return;
        var weapon = handler.selectedWeapon();
        if (weapon.isEmpty()) return;
        for (var index = 0; index < GorgeousSmithingTableMenu.MATERIAL_SLOT_COUNT; index++)
            previewMaterials.add(handler.getSlot(GorgeousSmithingTableMenu.FIRST_MATERIAL_SLOT + index).getStack());
        preview = WeaponEnhancement.preview(weapon, previewMaterials, 0.0D);
    }

    private void renderWeaponGrid(DrawContext graphics) {
        var grid = layout.weaponGrid;
        var cell = layout.weaponCell;
        well(graphics, layout.weaponWell);
        if (weaponRows.isEmpty()) {
            graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable(EMPTY_WEAPONS_KEY),
                    layout.weaponWell.centerX(), layout.weaponWell.centerY() - 4, SCRIM_TEXT);
            return;
        }

        var firstRow = MathHelper.floor(weaponScroll.shown());
        var top = Math.round(grid.y() - (weaponScroll.shown() - firstRow) * cell);
        var selected = handler.selectedIndex();
        graphics.enableScissor(grid.x(), grid.y(), grid.right(), grid.bottom());
        for (var row = 0; row <= layout.weaponRows; row++) {
            var yPos = top + row * cell;
            for (var column = 0; column < layout.weaponColumns; column++) {
                var index = (firstRow + row) * layout.weaponColumns + column;
                if (index >= weaponRows.size()) break;
                var xPos = grid.x() + column * cell;
                var chosen = selected == weaponIndices.get(index);
                slotCell(graphics, xPos, yPos, cell, cell, chosen ? FRAME_CHOSEN : FRAME_HOVER,
                        chosen ? 1.0F : index == hoveredWeaponCell ? weaponHover : 0.0F);
                var stack = weaponRows.get(index);
                renderLargeItem(graphics, stack, xPos, yPos, cell);
                graphics.drawItemInSlot(textRenderer, stack, xPos + cell / ITEM_SCALE, yPos + cell / ITEM_SCALE);
                renderMaxTag(graphics, stack, xPos, yPos, cell);
            }
        }
        graphics.disableScissor();
        scrollbar(graphics, layout.weaponScrollX, grid, weaponScroll, layout.weaponRows, weaponRowCount());
    }

    private void renderMaterialGrid(DrawContext graphics) {
        var grid = layout.materialGrid;
        var cell = layout.materialCell;
        well(graphics, layout.materialWell);
        if (materialRows.isEmpty()) {
            graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable(NO_MATERIALS_KEY),
                    layout.materialWell.centerX(), layout.materialWell.centerY() - 4, SCRIM_TEXT);
            return;
        }

        var firstRow = MathHelper.floor(materialScroll.shown());
        var top = Math.round(grid.y() - (materialScroll.shown() - firstRow) * cell);
        graphics.enableScissor(grid.x(), grid.y(), grid.right(), grid.bottom());
        for (var row = 0; row <= layout.materialRows; row++) {
            var yPos = top + row * cell;
            for (var column = 0; column < layout.materialColumns; column++) {
                var index = (firstRow + row) * layout.materialColumns + column;
                if (index >= materialRows.size()) break;
                var xPos = grid.x() + column * cell;
                var cellRow = materialRows.get(index);
                slotCell(graphics, xPos, yPos, cell, cell, FRAME_HOVER, index == hoveredMaterialCell ? materialHover : 0.0F);
                if (cellRow.linked()) graphics.fill(xPos, yPos, xPos + cell, yPos + cell, LINK_TINT);
                if (cellRow.stack().isEmpty()) continue;
                renderLargeItem(graphics, cellRow.stack(), xPos, yPos, cell);
                graphics.drawItemInSlot(textRenderer, cellRow.stack(), xPos + cell / ITEM_SCALE, yPos + cell / ITEM_SCALE);
            }
        }
        graphics.disableScissor();
        scrollbar(graphics, layout.materialScrollX, grid, materialScroll, layout.materialRows, materialRowCount());
    }

    private void renderPreview(DrawContext graphics) {
        var panel = layout.previewPanel;
        var weapon = handler.selectedWeapon();
        if (weapon.isEmpty()) {
            graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable(NO_WEAPON_KEY), panel.centerX(), panel.centerY() - 4, SCRIM_TEXT);
            return;
        }

        var state = WeaponState.of(weapon);
        var scale = layout.previewItemScale;
        var itemSize = 16 * scale;
        var block = itemSize + 55;
        var top = panel.y() + Math.max(12, (panel.height() - block) / 2);
        var pose = graphics.getMatrices();
        pose.push();
        pose.translate(panel.centerX(), top + itemSize / 2.0F, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawItem(weapon, -8, -8);
        pose.pop();

        var room = panel.width() - TEXT_INSET * 2;
        var nameY = top + itemSize + 10;
        graphics.drawCenteredTextWithShadow(textRenderer, ellipsized(weapon.getName(), room), panel.centerX(), nameY, TEXT_COLOR);
        graphics.drawCenteredTextWithShadow(textRenderer, ellipsized(Text.translatable(WeaponType.of(weapon).labelKey()), room), panel.centerX(), nameY + 12, MUTED_COLOR);
        graphics.drawCenteredTextWithShadow(textRenderer, levelLabel(state.level()), panel.centerX(), nameY + 24, WHITE_TEXT);
        renderRefinementRow(graphics, panel.centerX(), nameY + 36, state.refinements(),
                WeaponEnhancement.extraRefinement(client.player, weapon), 0, true);
    }

    private void renderRefinementRow(DrawContext graphics, int anchorX, int yPos, int refinement, int extra, int gained, boolean centered) {
        var label = Text.translatable(REFINEMENT_LABEL_KEY);
        var total = refinement + extra;
        var shown = total > MAX_STARS ? BASE_STARS : total;
        var normal = Math.min(refinement, shown);
        var labelWidth = textRenderer.getWidth(label);
        var startX = centered ? anchorX - (labelWidth + STAR_GAP + StarRow.width(shown)) / 2 : anchorX;
        graphics.drawText(textRenderer, label, startX, yPos, MUTED_COLOR, false);
        var starsX = startX + labelWidth + STAR_GAP;
        if (shown <= 0) {
            graphics.drawText(textRenderer, Integer.toString(refinement), starsX, yPos, MUTED_COLOR, false);
            return;
        }
        StarRow.render(graphics, starsX, yPos + 1, normal, shown - normal);
        if (gained <= 0) return;
        graphics.setShaderColor(1.0F, 1.0F, 1.0F, pulseAlpha());
        StarRow.render(graphics, starsX + StarRow.width(shown) + STAR_GAP, yPos + 1, gained, 0);
        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderLevel(DrawContext graphics) {
        var panel = layout.levelPanel;
        var weapon = handler.selectedWeapon();
        if (weapon.isEmpty()) {
            graphics.drawCenteredTextWithShadow(textRenderer, Text.translatable(NO_WEAPON_KEY), panel.centerX(), panel.centerY() - 4, SCRIM_TEXT);
            return;
        }

        var state = WeaponState.of(weapon);
        var cap = WeaponEnhancement.maxLevel(state.refinements());
        var maxed = state.level() >= cap;
        var requirement = WeaponEnhancement.requirement(state.level());
        var target = maxed ? 1.0F : MathHelper.clamp(state.xp() / (float) requirement, 0.0F, 1.0F);
        experienceFill = MathHelper.lerp(1.0F - (float) Math.exp(-LEVEL_RESPONSE * frameSeconds()), experienceFill, target);

        var projected = experienceFill;
        var projectedLevel = state.level();
        var projectedXp = state.xp();
        var refinementGain = 0;
        var attackGain = 0.0D;
        if (!maxed && preview != null) {
            refinementGain = Math.max(0, preview.refinement() - state.refinements());
            projectedLevel = MathHelper.clamp(preview.level(), state.level(), cap);
            projectedXp = projectedLevel == preview.level() ? preview.xp() : 0;
            projected = MathHelper.clamp(projectedXp / (float) WeaponEnhancement.requirement(projectedLevel), 0.0F, 1.0F);
            if (projectedLevel > state.level() && client.player != null)
                attackGain = WeaponAttributes.levelAttackBonus(weapon, client.player, projectedLevel)
                        - WeaponAttributes.levelAttackBonus(weapon, client.player, state.level());
        }

        var selection = handler.selectedIndex();
        if (selection != observedSelection) {
            observedSelection = selection;
            observedLevel = -1;
            endLevelUp();
        }
        var gainedLevel = observedLevel >= 0 && state.level() > observedLevel;
        var origin = gainedLevel ? MathHelper.clamp(observedXp / (float) WeaponEnhancement.requirement(observedLevel), 0.0F, 1.0F) : target;
        if (gainedLevel) beginLevelUp(true, displayedFill(origin), target, state.level(), state.xp());
        else advanceLevelUpPreview(projectedLevel > state.level(), target, projected, projectedLevel, projectedXp);
        observedLevel = state.level();
        observedXp = state.xp();

        if (projectedLevel > state.level()) {
            graphics.drawText(textRenderer, Text.translatable(LEVEL_PREVIEW_KEY, levelLabel(state.level()), levelLabel(projectedLevel)),
                    panel.x() + TEXT_INSET, layout.levelTextY, WHITE_TEXT, false);
        } else {
            graphics.drawText(textRenderer, levelLabel(state.level()), panel.x() + TEXT_INSET, layout.levelTextY, WHITE_TEXT, false);
        }
        var trailing = maxed ? Text.translatable(MAX_LEVEL_KEY)
                : Text.translatable(REMAINING_KEY, WeaponEnhancement.requirement(projectedLevel) - projectedXp);
        drawRight(graphics, trailing, panel.right() - TEXT_INSET, layout.levelTextY);

        renderRefinementRow(graphics, panel.x() + TEXT_INSET, layout.levelPreviewY, state.refinements(),
                WeaponEnhancement.extraRefinement(client.player, weapon), refinementGain, false);
        if (attackGain > 1.0E-4D) {
            drawRight(graphics, Text.translatable(ATTACK_PREVIEW_KEY, WishReports.number(attackGain, Formatting.AQUA)),
                    panel.right() - TEXT_INSET, layout.levelPreviewY);
        }

        var bar = layout.levelBar;
        graphics.fill(bar.x(), bar.y(), bar.right(), bar.bottom(), BAR_EDGE);
        graphics.fill(bar.x() + 1, bar.y() + 1, bar.right() - 1, bar.bottom() - 1, BAR_TRACK);
        var span = bar.width() - 2;
        var projectedFill = levelUpActive ? levelUpFill() : projected;
        var head = Math.round(span * MathHelper.clamp(projectedFill, 0.0F, 1.0F));
        var filled = Math.round(span * MathHelper.clamp(experienceFill, 0.0F, 1.0F));
        if (levelUpReal) filled = head;
        else if (levelUpEnteredNextLevel()) filled = 0;
        if (Math.max(filled, head) > 0) graphics.fill(bar.x() + 1, bar.y() + 1, bar.x() + 1 + Math.max(filled, head), bar.bottom() - 1, BAR_FILL);
        if (head > filled) {
            graphics.fill(bar.x() + 1 + filled, bar.y() + 1, bar.x() + 1 + head, bar.bottom() - 1, withAlpha(pulseAlpha()));
        }
    }

    private void beginLevelUp(boolean real, float origin, float result, int level, int xp) {
        levelUpActive = true;
        levelUpReal = real;
        levelUpOrigin = origin;
        levelUpResult = result;
        levelUpLevel = level;
        levelUpXp = xp;
        levelUpSeconds = 0.0F;
    }

    private float displayedFill(float fallback) {
        return levelUpActive && !levelUpReal ? levelUpFill() : fallback;
    }

    private void endLevelUp() {
        levelUpActive = false;
        levelUpReal = false;
    }

    private void advanceLevelUpPreview(boolean wanted, float origin, float result, int level, int xp) {
        if (levelUpReal) {
            if (levelUpSeconds >= LEVEL_UP_END_SECONDS) endLevelUp();
            else levelUpSeconds = Math.min(levelUpSeconds + frameSeconds(), LEVEL_UP_END_SECONDS);
            return;
        }
        if (!wanted) {
            levelUpActive = false;
            return;
        }
        if (!levelUpActive || origin != levelUpOrigin || level != levelUpLevel || xp != levelUpXp) {
            beginLevelUp(false, origin, result, level, xp);
            return;
        }
        levelUpSeconds = Math.min(levelUpSeconds + frameSeconds(), LEVEL_UP_END_SECONDS);
    }

    private float levelUpFill() {
        if (levelUpSeconds < LEVEL_UP_TRAVEL_SECONDS)
            return MathHelper.lerp(eased(levelUpSeconds / LEVEL_UP_TRAVEL_SECONDS), levelUpOrigin, 1.0F);
        if (levelUpSeconds < LEVEL_UP_NEXT_SECONDS) return 1.0F;
        var next = levelUpSeconds - LEVEL_UP_NEXT_SECONDS;
        if (next < LEVEL_UP_TRAVEL_SECONDS) return MathHelper.lerp(eased(next / LEVEL_UP_TRAVEL_SECONDS), 0.0F, levelUpResult);
        return levelUpResult;
    }

    private boolean levelUpEnteredNextLevel() {
        return levelUpActive && !levelUpReal && levelUpSeconds >= LEVEL_UP_NEXT_SECONDS;
    }

    private static float eased(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }

    private void renderButtons(DrawContext graphics) {
        var fill = layout.fillButton;
        NineSliceButton.draw(graphics, textRenderer, fill.x(), fill.y(), fill.width(), fill.height(),
                Text.translatable(FILL_KEY), handler.canFill(), fillHover > 0.5F);

        var enhance = layout.enhanceButton;
        if (armed) graphics.setShaderColor(ARMED_TINT_RED, ARMED_TINT_GREEN, ARMED_TINT_BLUE, 1.0F);
        NineSliceButton.draw(graphics, textRenderer, enhance.x(), enhance.y(), enhance.width(), enhance.height(),
                Text.translatable(armed ? CONFIRM_AGAIN_KEY : ENHANCE_KEY), handler.canEnhance(), buttonHover > 0.5F && !buttonPressed);
        if (armed) graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderHints(DrawContext graphics) {
        var area = layout.hintArea;
        if (area.height() <= 0) return;
        var yPos = area.y();
        var drawn = 0;
        for (var key : hintKeys()) {
            for (var line : textRenderer.wrapLines(Text.translatable(key), area.width())) {
                if (drawn >= HINT_MAX_LINES || yPos + textRenderer.fontHeight > area.bottom()) return;
                graphics.drawCenteredTextWithShadow(textRenderer, line, area.centerX(), yPos, WHITE_TEXT);
                yPos += textRenderer.fontHeight + 2;
                drawn++;
            }
        }
    }

    private List<String> hintKeys() {
        return handler.linkedSlotCount() > 0 ? List.of(SELECT_HINT_KEY, LINKED_HINT_KEY) : List.of(SELECT_HINT_KEY);
    }

    private void renderHoverTooltip(DrawContext graphics, int mouseX, int mouseY) {
        if (!handler.getCursorStack().isEmpty()) return;
        var stack = hoveredStack(mouseX, mouseY);
        if (!stack.isEmpty()) {
            var lines = new ArrayList<>(getTooltipFromItem(client, stack));
            if (linkedRowAt(mouseX, mouseY)) lines.add(Text.translatable(LINKED_SOURCE_KEY));
            graphics.drawTooltip(textRenderer, lines, stack.getTooltipData(), mouseX, mouseY);
            return;
        }
        if (layout.enhanceButton.contains(mouseX, mouseY)) {
            if (armed) graphics.drawTooltip(textRenderer, confirmMessage(), Optional.empty(), mouseX, mouseY);
            else graphics.drawTooltip(textRenderer, Text.translatable(ENHANCE_TOOLTIP_KEY), mouseX, mouseY);
            return;
        }
        if (layout.fillButton.contains(mouseX, mouseY)) {
            graphics.drawTooltip(textRenderer, Text.translatable(FILL_TOOLTIP_KEY), mouseX, mouseY);
        }
    }

    private List<Text> confirmMessage() {
        var lines = new ArrayList<Text>(CONFIRM_MESSAGE_KEYS.size());
        for (var key : CONFIRM_MESSAGE_KEYS) lines.add(Text.translatable(key));
        return lines;
    }

    private ItemStack hoveredStack(double mouseX, double mouseY) {
        if (hoveredMaterialSlot >= 0) return handler.getSlot(GorgeousSmithingTableMenu.FIRST_MATERIAL_SLOT + hoveredMaterialSlot).getStack();
        var weaponCell = weaponCellAt(mouseX, mouseY);
        if (weaponCell >= 0) return weaponRows.get(weaponCell);
        var materialCell = materialCellAt(mouseX, mouseY);
        if (materialCell >= 0) return materialRows.get(materialCell).stack();
        if (layout.previewPanel.contains(mouseX, mouseY)) return handler.selectedWeapon();
        return ItemStack.EMPTY;
    }

    private boolean linkedRowAt(double mouseX, double mouseY) {
        if (hoveredMaterialSlot >= 0) return false;
        if (weaponCellAt(mouseX, mouseY) >= 0) return false;
        var materialCell = materialCellAt(mouseX, mouseY);
        return materialCell >= 0 && materialRows.get(materialCell).linked();
    }

    private int weaponCellAt(double mouseX, double mouseY) {
        return gridCellAt(layout.weaponGrid, layout.weaponColumns, layout.weaponCell, weaponScroll.shown(), weaponRows.size(), mouseX, mouseY);
    }

    private int materialCellAt(double mouseX, double mouseY) {
        return gridCellAt(layout.materialGrid, layout.materialColumns, layout.materialCell, materialScroll.shown(), materialRows.size(), mouseX, mouseY);
    }

    private int gridCellAt(Rect grid, int columns, int cell, float scroll, int total, double mouseX, double mouseY) {
        if (!grid.contains(mouseX, mouseY)) return -1;
        var column = (int) ((mouseX - grid.x()) / cell);
        if (column < 0 || column >= columns) return -1;
        var firstRow = MathHelper.floor(scroll);
        var local = mouseY - grid.y() + (scroll - firstRow) * cell;
        if (local < 0) return -1;
        var index = (firstRow + (int) (local / cell)) * columns + column;
        return index >= 0 && index < total ? index : -1;
    }

    private int slotIndexAt(double mouseX, double mouseY) {
        for (var index = 0; index < GorgeousSmithingTableMenu.MATERIAL_SLOT_COUNT; index++) {
            if (layout.materialSlot(index).contains(mouseX, mouseY)) return index;
        }
        return -1;
    }

    private Slot slotAt(double mouseX, double mouseY) {
        var index = slotIndexAt(mouseX, mouseY);
        return index < 0 ? null : handler.getSlot(GorgeousSmithingTableMenu.FIRST_MATERIAL_SLOT + index);
    }

    private void renderLargeItem(DrawContext graphics, ItemStack stack, int xPos, int yPos, int size) {
        var pose = graphics.getMatrices();
        pose.push();
        pose.translate(xPos + size / 2.0F, yPos + size / 2.0F, 0.0F);
        pose.scale(ITEM_SCALE, ITEM_SCALE, 1.0F);
        graphics.drawItem(stack, -8, -8);
        pose.pop();
    }

    private void renderMaxTag(DrawContext graphics, ItemStack stack, int xPos, int yPos, int size) {
        var color = maxTagColor(stack);
        if (color == 0) return;
        var text = Text.translatable(MAX_TAG_KEY);
        var pose = graphics.getMatrices();
        pose.push();
        pose.translate(xPos + size - MAX_TAG_INSET, yPos + size - MAX_TAG_INSET, 0.0F);
        pose.scale(MAX_TAG_SCALE, MAX_TAG_SCALE, 1.0F);
        graphics.drawText(textRenderer, text, -textRenderer.getWidth(text), -textRenderer.fontHeight, color, true);
        pose.pop();
    }

    private int maxTagColor(ItemStack stack) {
        var state = WeaponState.of(stack);
        var refined = state.refinements() >= WeaponEnhancement.MAX_REFINEMENT;
        var leveled = state.level() >= WeaponEnhancement.maxLevel(state.refinements());
        if (leveled && refined) return MAX_TAG_FULL_COLOR;
        if (leveled) return MAX_TAG_LEVEL_COLOR;
        return refined ? MAX_TAG_REFINEMENT_COLOR : 0;
    }

    private void plate(DrawContext graphics, Rect rect) {
        frame(graphics, PANEL, PANEL_INSET, rect);
    }

    private void well(DrawContext graphics, Rect rect) {
        frame(graphics, WELL, WELL_INSET, rect);
    }

    private void frame(DrawContext graphics, Sprite slice, int inset, Rect rect) {
        var rectWidth = rect.width();
        var rectHeight = rect.height();
        if (rectWidth <= 0 || rectHeight <= 0) return;
        var corner = Math.min(inset, Math.min(rectWidth, rectHeight) / 2);
        if (corner <= 0) {
            blit(graphics, rect.x(), rect.y(), rectWidth, rectHeight, slice.u(), slice.v(), slice.size(), slice.size());
            return;
        }
        var xPos = rect.x();
        var yPos = rect.y();
        var rightX = xPos + rectWidth - corner;
        var bottomY = yPos + rectHeight - corner;
        var centerWidth = rectWidth - corner * 2;
        var centerHeight = rectHeight - corner * 2;
        var u = slice.u();
        var v = slice.v();
        var uRight = u + slice.size() - corner;
        var vBottom = v + slice.size() - corner;
        var face = slice.size() - corner * 2;
        blit(graphics, xPos, yPos, corner, corner, u, v, corner, corner);
        blit(graphics, rightX, yPos, corner, corner, uRight, v, corner, corner);
        blit(graphics, xPos, bottomY, corner, corner, u, vBottom, corner, corner);
        blit(graphics, rightX, bottomY, corner, corner, uRight, vBottom, corner, corner);
        if (centerWidth <= 0 && centerHeight <= 0) return;
        if (centerWidth > 0) {
            blit(graphics, xPos + corner, yPos, centerWidth, corner, u + corner, v, face, corner);
            blit(graphics, xPos + corner, bottomY, centerWidth, corner, u + corner, vBottom, face, corner);
        }
        if (centerHeight > 0) {
            blit(graphics, xPos, yPos + corner, corner, centerHeight, u, v + corner, corner, face);
            blit(graphics, rightX, yPos + corner, corner, centerHeight, uRight, v + corner, corner, face);
        }
        if (centerWidth > 0 && centerHeight > 0)
            blit(graphics, xPos + corner, yPos + corner, centerWidth, centerHeight, u + corner, v + corner, face, face);
    }

    private void slotCell(DrawContext graphics, int xPos, int yPos, int cellWidth, int cellHeight, Sprite overlay, float amount) {
        blitCell(graphics, GorgeousSmithingTableScreen.FRAME, xPos, yPos, cellWidth, cellHeight);
        if (amount <= HIGHLIGHT_FLOOR) return;
        graphics.setShaderColor(1.0F, 1.0F, 1.0F, MathHelper.clamp(amount, 0.0F, 1.0F));
        blitCell(graphics, overlay, xPos, yPos, cellWidth, cellHeight);
        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void scrollbar(DrawContext graphics, int xPos, Rect grid, SmoothScroll scroll, int visibleRows, int totalRows) {
        if (totalRows <= visibleRows) return;
        graphics.fill(xPos, grid.y(), xPos + SCROLLBAR_WIDTH, grid.bottom(), SCROLL_TRACK);
        var thumb = Math.max(SCROLLBAR_MIN_THUMB, Math.round(grid.height() * (visibleRows / (float) totalRows)));
        var travel = grid.height() - thumb;
        var offset = grid.y() + Math.round(travel * scroll.progress());
        graphics.fill(xPos, offset, xPos + SCROLLBAR_WIDTH, offset + thumb, SCROLL_THUMB);
    }

    private void drawRight(DrawContext graphics, Text text, int right, int yPos) {
        graphics.drawText(textRenderer, text, right - textRenderer.getWidth(text), yPos, GorgeousSmithingTableScreen.MUTED_COLOR, false);
    }

    private OrderedText ellipsized(Text text, int maxWidth) {
        if (textRenderer.getWidth(text) <= maxWidth) return Language.getInstance().reorder(text);
        var room = Math.max(0, maxWidth - textRenderer.getWidth(ELLIPSIS));
        return Language.getInstance().reorder(
                StringVisitable.concat(StringVisitable.plain(textRenderer.trimToWidth(text.getString(), room)), StringVisitable.plain(ELLIPSIS)));
    }

    private Text levelLabel(int level) {
        return Text.translatable(LEVEL_KEY, Text.literal(Integer.toString(level)));
    }

    private int weaponRowCount() {
        return (weaponRows.size() + layout.weaponColumns - 1) / layout.weaponColumns;
    }

    private int materialRowCount() {
        return (materialRows.size() + layout.materialColumns - 1) / layout.materialColumns;
    }

    private float pulseAlpha() {
        return PULSE_MIN + (1.0F - PULSE_MIN) * (0.5F + 0.5F * MathHelper.sin(pulse));
    }

    private float frameSeconds() {
        if (client == null) return 0.0F;
        return MathHelper.clamp(client.getLastFrameDuration(), 0.0F, 4.0F) * 0.05F;
    }

    private void click(int id) {
        if (client == null || client.interactionManager == null) return;
        client.interactionManager.clickButton(handler.syncId, id);
    }

    private void playSound(SoundEvent event) {
        if (client == null) return;
        client.getSoundManager().play(PositionedSoundInstance.master(event, 1.0F));
    }

    private void blitCell(DrawContext graphics, Sprite sprite, int xPos, int yPos, int cellWidth, int cellHeight) {
        blit(graphics, xPos, yPos, cellWidth, cellHeight, sprite.u(), sprite.v(), sprite.size(), sprite.size());
    }

    private void blit(DrawContext graphics, int xPos, int yPos, int drawWidth, int drawHeight, int u, int v, int sourceWidth, int sourceHeight) {
        if (drawWidth <= 0 || drawHeight <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.drawTexture(ATLAS, xPos, yPos, drawWidth, drawHeight, u, v, sourceWidth, sourceHeight, ATLAS_WIDTH, ATLAS_HEIGHT);
    }

    private record Sprite(int u, int v, int size) {
    }

    private record MaterialRow(ItemStack stack, int index, boolean linked) {
    }

    private record RowKey(boolean linked, int index) {
    }

    private static int withAlpha(float amount) {
        var alpha = Math.round(((GorgeousSmithingTableScreen.BAR_PREVIEW >>> 24) & 0xFF) * MathHelper.clamp(amount, 0.0F, 1.0F));
        return alpha << 24 | GorgeousSmithingTableScreen.BAR_PREVIEW & 0xFFFFFF;
    }
}