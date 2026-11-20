package net.per.primogemcraft.client.gui.teyvat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.per.primogemcraft.client.gui.NineSliceButton;

import java.util.function.BooleanSupplier;

public final class TeyvatButton extends Button {
    private final BooleanSupplier selected;

    public TeyvatButton(int x, int y, int width, int height, Text title, OnPress action, BooleanSupplier selected) {
        super(x, y, width, height, title, action, DEFAULT_NARRATION);
        this.selected = selected;
    }

    @Override
    protected void renderWidget(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        var availableWidth = Math.max(0, getWidth() - 8);
        var text = font.width(getMessage()) <= availableWidth ? getMessage()
                : Text.literal(font.plainSubstrByWidth(getMessage().getString(),
                        Math.max(0, availableWidth - font.width(CommonComponents.ELLIPSIS)))).append(CommonComponents.ELLIPSIS);
        NineSliceButton.drawTeyvat(graphics, font, getX(), getY(), getWidth(), getHeight(), text, active,
                isHoveredOrFocused(), selected.getAsBoolean());
    }
}
