package net.per.primogemcraft.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.per.primogemcraft.system.weapon.WeaponTooltip;

public class WeaponTooltipRenderer implements ClientTooltipComponent {
    private static final String LEVEL_KEY = "weapon.primogemcraft.level";
    private static final String REFINEMENT_KEY = "weapon.primogemcraft.refinement";
    private static final String REFINEMENT_EXTRA_KEY = "weapon.primogemcraft.refinement.extra";
    private static final String STACKS_KEY = "weapon.primogemcraft.stacks";

    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int EXTRA_COLOR = 0x55FFFF;
    private static final int LABEL_GAP = 2;
    private static final int STAR_TOP = 1;
    private static final int BASE_STARS = 5;
    private static final int MAX_STARS = 8;
    private static final int ROWS = 2;

    private final int level;
    private final int refinement;
    private final int extra;
    private final int stacks;
    private final int stackCapacity;

    public WeaponTooltipRenderer(WeaponTooltip tooltip) {
        level = tooltip.level();
        refinement = tooltip.refinement();
        extra = tooltip.extra();
        stacks = tooltip.stacks();
        stackCapacity = tooltip.stackCapacity();
    }

    @Override
    public int getHeight() {
        return LINE_HEIGHT * (ROWS + (hasStacks() ? 1 : 0));
    }

    @Override
    public int getWidth(Font font) {
        var width = Math.max(font.width(levelLine()), refinementRowWidth(font));
        return hasStacks() ? Math.max(width, stacksRowWidth(font)) : width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        graphics.drawString(font, levelLine(), x, y, TEXT_COLOR, true);
        var rowY = y + LINE_HEIGHT;
        renderRefinement(font, graphics, x, rowY);
        if (hasStacks()) renderStacks(font, graphics, x, rowY + LINE_HEIGHT);
    }

    private void renderRefinement(Font font, GuiGraphics graphics, int x, int y) {
        var label = Component.translatable(REFINEMENT_KEY);
        graphics.drawString(font, label, x, y, TEXT_COLOR, true);
        var rowX = x + font.width(label) + LABEL_GAP;
        if (refinement <= 0) {
            graphics.drawString(font, Component.literal(number()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), rowX, y, TEXT_COLOR, true);
            return;
        }
        StarRow.render(graphics, rowX, y + STAR_TOP, normalStars(), fusionStars());
        if (overflow() > 0)
            graphics.drawString(font, overflowLine(), rowX + starsWidth() + LABEL_GAP, y, EXTRA_COLOR, true);
    }

    private void renderStacks(Font font, GuiGraphics graphics, int x, int y) {
        var label = Component.translatable(STACKS_KEY);
        graphics.drawString(font, label, x, y, TEXT_COLOR, true);
        var full = stacks >= stackCapacity;
        StarRow.render(graphics, x + font.width(label) + LABEL_GAP, y + STAR_TOP, full ? 0 : stacks, full ? stacks : 0);
    }

    private int refinementRowWidth(Font font) {
        var width = font.width(Component.translatable(REFINEMENT_KEY)) + LABEL_GAP + starsWidth(font);
        return overflow() > 0 ? width + LABEL_GAP + font.width(overflowLine()) : width;
    }

    private int stacksRowWidth(Font font) {
        return font.width(Component.translatable(STACKS_KEY)) + LABEL_GAP + StarRow.width(stacks);
    }

    private int starsWidth() {
        return StarRow.width(shownStars());
    }

    private int starsWidth(Font font) {
        return refinement <= 0 ? font.width(number()) : starsWidth();
    }

    private int total() {
        return refinement + extra;
    }

    private int shownStars() {
        return total() > MAX_STARS ? BASE_STARS : total();
    }

    private int normalStars() {
        return Math.min(refinement, shownStars());
    }

    private int fusionStars() {
        return shownStars() - normalStars();
    }

    private int overflow() {
        return total() > MAX_STARS ? total() - BASE_STARS : 0;
    }

    private boolean hasStacks() {
        return stackCapacity > 0 && stacks > 0;
    }

    private Component levelLine() {
        var text = Component.translatable(LEVEL_KEY, Component.literal(Integer.toString(level)));
        return level <= 0 ? text.withStyle(ChatFormatting.RED, ChatFormatting.BOLD) : text;
    }

    private Component overflowLine() {
        return Component.translatable(REFINEMENT_EXTRA_KEY, overflow());
    }

    private String number() {
        return Integer.toString(refinement);
    }
}
