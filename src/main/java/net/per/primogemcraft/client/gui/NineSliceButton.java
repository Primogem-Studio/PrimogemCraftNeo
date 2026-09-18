package net.per.primogemcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class NineSliceButton {
    public static final int TEXT_COLOR = 0xFFE5C99C;

    private static final int TEXT_HOVER_COLOR = 0xFF2D313F;
    private static final int TEXTURE_WIDTH = 20;
    private static final int TEXTURE_HEIGHT = 25;
    private static final int PRESS_SINK = 1;
    private static final int TEXT_DROP = 1;

    private static final Skin NORMAL = new Skin(texture("u_button"), 2, 2, 2, 6);
    private static final Skin HIGHLIGHT = new Skin(texture("u_button_press"), 2, 2, 2, 4);
    private static final Skin DISABLED = new Skin(texture("u_button_no"), 2, 2, 2, 6);

    private NineSliceButton() {
    }

    public static void draw(GuiGraphics graphics, Font font, int x, int y, int width, int height, Component text,
                            boolean enabled, boolean hover) {
        var skin = enabled ? hover ? HIGHLIGHT : NORMAL : DISABLED;
        var bottom = fit(skin, height, font.lineHeight);
        skin(graphics, skin, x, y, width, height, bottom);
        var faceWidth = Math.max(1, width - skin.left() - skin.right());
        var faceHeight = Math.max(0, height - skin.top() - bottom);
        var sink = enabled && hover && leftPressed() ? PRESS_SINK : 0;
        var textX = x + skin.left() + (faceWidth - font.width(text)) / 2;
        var textY = y + skin.top() + (faceHeight - font.lineHeight) / 2 + TEXT_DROP + sink;
        graphics.drawString(font, text, textX, textY, enabled && hover ? TEXT_HOVER_COLOR : TEXT_COLOR, false);
    }

    public static void draw(GuiGraphics graphics, Font font, int x, int y, int width, int height, Component text, boolean enabled) {
        draw(graphics, font, x, y, width, height, text, enabled, false);
    }

    public static void draw(GuiGraphics graphics, Font font, GorgeousSmithingTableLayout.Rect rect, Component text, boolean enabled) {
        draw(graphics, font, rect.x(), rect.y(), rect.width(), rect.height(), text, enabled, false);
    }

    private static int fit(Skin skin, int height, int lineHeight) {
        return Math.max(0, Math.min(skin.bottom(), height - skin.top() - lineHeight));
    }

    private static boolean leftPressed() {
        var minecraft = Minecraft.getInstance();
        return minecraft.mouseHandler != null && minecraft.mouseHandler.isLeftPressed();
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/widget/" + name + ".png");
    }

    private static void skin(GuiGraphics graphics, Skin skin, int x, int y, int width, int height, int bottomInset) {
        var left = skin.left();
        var right = skin.right();
        var top = skin.top();
        var bottom = Math.min(bottomInset, skin.bottom());
        var centerWidth = Math.max(0, width - left - right);
        var centerHeight = Math.max(0, height - top - bottom);
        var faceWidth = skin.faceWidth();
        var faceHeight = skin.faceHeight();
        var bandTop = TEXTURE_HEIGHT - skin.bottom();
        var rightX = x + width - right;
        var bottomY = y + height - bottom;
        var texture = skin.texture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (centerHeight > 0) {
            graphics.blit(texture, x, y + top, left, centerHeight, 0, top, left, faceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            graphics.blit(texture, rightX, y + top, right, centerHeight, TEXTURE_WIDTH - right, top, right, faceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        if (centerWidth > 0) {
            graphics.blit(texture, x + left, y, centerWidth, top, left, 0, faceWidth, top, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            graphics.blit(texture, x + left, bottomY, centerWidth, bottom, left, bandTop, faceWidth, bottom, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        if (centerWidth > 0 && centerHeight > 0) {
            graphics.blit(texture, x + left, y + top, centerWidth, centerHeight, left, top, faceWidth, faceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        graphics.blit(texture, x, y, left, top, 0, 0, left, top, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(texture, rightX, y, right, top, TEXTURE_WIDTH - right, 0, right, top, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(texture, x, bottomY, left, bottom, 0, bandTop, left, bottom, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.blit(texture, rightX, bottomY, right, bottom, TEXTURE_WIDTH - right, bandTop, right, bottom, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private record Skin(ResourceLocation texture, int left, int right, int top, int bottom) {
        int faceWidth() {
            return TEXTURE_WIDTH - left - right;
        }

        int faceHeight() {
            return TEXTURE_HEIGHT - top - bottom;
        }
    }
}
