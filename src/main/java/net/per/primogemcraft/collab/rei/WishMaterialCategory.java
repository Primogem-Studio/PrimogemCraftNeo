package net.per.primogemcraft.collab.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.per.primogemcraft.client.gui.GuiAtlas;
import net.per.primogemcraft.registry.PGCItems;

import java.util.ArrayList;
import java.util.List;

public class WishMaterialCategory implements DisplayCategory<WishMaterialDisplay> {
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 7;

    @Override
    public List<Widget> setupDisplay(WishMaterialDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();
        var left = bounds.getCenterX() - WishMaterialDisplay.COLUMNS * SLOT_SIZE / 2;
        var top = bounds.y + PADDING;
        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            GuiAtlas.panel(graphics, bounds.x, bounds.y, bounds.width, bounds.height);
            for (var index = 0; index < WishMaterialDisplay.PAGE_SIZE; index++) {
                GuiAtlas.sprite(graphics, GuiAtlas.SLOT, left + index % WishMaterialDisplay.COLUMNS * SLOT_SIZE,
                        top + index / WishMaterialDisplay.COLUMNS * SLOT_SIZE);
            }
        }));
        for (var index = 0; index < display.getInputEntries().size(); index++) {
            var point = new Point(left + index % WishMaterialDisplay.COLUMNS * SLOT_SIZE + 1,
                    top + index / WishMaterialDisplay.COLUMNS * SLOT_SIZE + 1);
            var slot = Widgets.createSlot(point).disableBackground().markInput();
            for (var entry : display.getInputEntries().get(index)) {
                slot.entry(entry.copy().tooltip(Component.translatable("rei.primogemcraft.wish_materials.tooltip.0", display.value(index))
                        .withStyle(ChatFormatting.AQUA)));
            }
            widgets.add(slot);
        }
        return widgets;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("rei.category.primogemcraft.wish_materials");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(PGCItems.WISH_CORE.get());
    }

    @Override
    public int getDisplayHeight() {
        return WishMaterialDisplay.ROWS * SLOT_SIZE + PADDING * 2;
    }

    @Override
    public int getDisplayWidth(WishMaterialDisplay display) {
        return WishMaterialDisplay.COLUMNS * SLOT_SIZE + PADDING * 2;
    }

    @Override
    public CategoryIdentifier<? extends WishMaterialDisplay> getCategoryIdentifier() {
        return PGCREIPlugin.WISH_MATERIALS;
    }
}
