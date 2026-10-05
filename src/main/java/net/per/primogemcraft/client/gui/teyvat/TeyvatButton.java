package net.per.primogemcraft.client.gui.teyvat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.per.primogemcraft.client.gui.NineSliceButton;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public final class TeyvatButton extends Button {
    private final BooleanSupplier selected;

    public TeyvatButton(int x, int y, int width, int height, Component title, OnPress action, BooleanSupplier selected) {
        super(x, y, width, height, title, action, DEFAULT_NARRATION);
        this.selected = selected;
        setTooltip(Tooltip.create(title));
        setTooltipDelay(Duration.ofMillis(300));
    }

    @Override
    public void setMessage(Component message) {
        if (message.getContents() instanceof TranslatableContents contents
                && contents.getKey().equals("gui.teyvatdelight.katheryne.buy")
                && Minecraft.getInstance().screen instanceof KatheryneCollaboration.Host host
                && host.primogemcraft$collaboration() != null && host.primogemcraft$collaboration().taskPage())
            message = Component.translatable("gui.primogemcraft.stellar_tasks.submit");
        if (message.equals(getMessage())) return;
        super.setMessage(message);
        setTooltip(Tooltip.create(message));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var font = Minecraft.getInstance().font;
        var availableWidth = Math.max(0, getWidth() - 8);
        var text = font.width(getMessage()) <= availableWidth ? getMessage()
                : Component.literal(font.plainSubstrByWidth(getMessage().getString(),
                        Math.max(0, availableWidth - font.width(CommonComponents.ELLIPSIS)))).append(CommonComponents.ELLIPSIS);
        NineSliceButton.drawTeyvat(graphics, font, getX(), getY(), getWidth(), getHeight(), text, active,
                isHoveredOrFocused(), selected.getAsBoolean());
    }
}
