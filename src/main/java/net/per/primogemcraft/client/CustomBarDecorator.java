package net.per.primogemcraft.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.registry.PGCDataComponents;

public class CustomBarDecorator implements IItemDecorator {
    private final int color;
    private final boolean depleting;

    public CustomBarDecorator(int color, boolean depleting) {
        this.color = color;
        this.depleting = depleting;
    }

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        if (bar == null || !bar.visible()) return false;
        ItemBar.render(graphics, x, y, ItemBar.DURABILITY_TOP, ItemBar.filled(points(bar), bar.denominator()), color);
        return true;
    }

    private int points(CustomBar bar) {
        return depleting ? bar.remaining() : bar.numerator();
    }
}
