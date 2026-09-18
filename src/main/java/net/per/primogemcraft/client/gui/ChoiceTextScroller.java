package net.per.primogemcraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class ChoiceTextScroller {
    private static final int LINE_SPACING = 1;
    private static final float SCROLL_RESPONSE = 22.0F;
    private static final float SCROLL_SETTLE = 0.01F;
    private static final float LINES_PER_NOTCH = 1.0F;

    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private int visibleLines = 1;
    private int lineHeight = 9;
    private float target;
    private float shown;

    public void wrap(int width, int height, List<FormattedText> content) {
        var font = Minecraft.getInstance().font;
        lines.clear();
        for (var text : content) lines.addAll(font.split(text, width));
        lineHeight = font.lineHeight + LINE_SPACING;
        visibleLines = Math.max(1, height / lineHeight);
        target = 0.0F;
        shown = 0.0F;
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public boolean isNotScrollable() {
        return lines.size() <= visibleLines;
    }

    public void scroll(double notches) {
        target = Mth.clamp((float) (target - notches * LINES_PER_NOTCH), 0.0F, maxScroll());
    }

    public void advance(float frameSeconds) {
        shown = Mth.lerp(1.0F - (float) Math.exp(-SCROLL_RESPONSE * frameSeconds), shown, target);
        if (Math.abs(target - shown) < SCROLL_SETTLE) shown = target;
    }

    public float progress() {
        var max = maxScroll();
        return max <= 0.0F ? 0.0F : Mth.clamp(shown / max, 0.0F, 1.0F);
    }

    public float thumbFraction() {
        return lines.isEmpty() ? 1.0F : (float) visibleLines / lines.size();
    }

    public void render(GuiGraphics graphics, int x, int y, int width, int color) {
        if (lines.isEmpty()) return;

        var font = Minecraft.getInstance().font;
        var first = Mth.floor(shown);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, -lineHeight * (shown - first), 0.0F);
        for (var index = 0; index <= visibleLines; index++) {
            var line = first + index;
            if (line >= lines.size()) break;
            var text = lines.get(line);
            graphics.drawString(font, text, x + (width - font.width(text)) / 2, y + index * lineHeight, color, false);
        }
        pose.popPose();
    }

    private float maxScroll() {
        return Math.max(0, lines.size() - visibleLines);
    }
}
