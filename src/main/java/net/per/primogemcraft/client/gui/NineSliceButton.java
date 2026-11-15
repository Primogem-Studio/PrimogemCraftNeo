package net.per.primogemcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * 按钮九宫格绘制（1.20.1 Yarn）。
 *
 * <p>原版用 {@code MouseHandler.isLeftPressed()} 判断按下下沉，
 * 1.20.1 的 Mouse 未公开该状态，这里简化为不做 1px 下沉，其余表现一致。
 */
public final class NineSliceButton {
    public static final int TEXT_COLOR = 0xFFE5C99C;

    private static final int TEXT_HOVER_COLOR = 0xFF2D313F;
    private static final int TEXTURE_WIDTH = 20;
    private static final int TEXTURE_HEIGHT = 25;
    private static final int TEXT_DROP = 1;

    private static final Skin NORMAL = new Skin(texture("u_button"), 2, 2, 2, 6);
    private static final Skin HIGHLIGHT = new Skin(texture("u_button_press"), 2, 2, 2, 4);
    private static final Skin DISABLED = new Skin(texture("u_button_no"), 2, 2, 2, 6);

    private NineSliceButton() {
    }

    public static void draw(DrawContext graphics, TextRenderer font, int x, int y, int width, int height, Text text,
                            boolean enabled, boolean hover) {
        draw(graphics, font, x, y, width, height, text, enabled, hover, false);
    }

    public static void draw(DrawContext graphics, TextRenderer font, int x, int y, int width, int height, Text text,
                            boolean enabled, boolean hover, boolean selected) {
        var highlighted = enabled && (hover || selected);
        var skin = enabled ? highlighted ? HIGHLIGHT : NORMAL : DISABLED;
        var bottom = fit(skin, height, font.fontHeight);
        skin(graphics, skin, x, y, width, height, bottom);
        var faceWidth = Math.max(1, width - skin.left() - skin.right());
        var faceHeight = Math.max(0, height - skin.top() - bottom);
        var textX = x + skin.left() + (faceWidth - font.getWidth(text)) / 2;
        var textY = y + skin.top() + (faceHeight - font.fontHeight) / 2 + TEXT_DROP;
        graphics.drawText(font, text, textX, textY, highlighted ? TEXT_HOVER_COLOR : TEXT_COLOR, false);
    }

    public static void draw(DrawContext graphics, TextRenderer font, int x, int y, int width, int height, Text text, boolean enabled) {
        draw(graphics, font, x, y, width, height, text, enabled, false);
    }

    private static int fit(Skin skin, int height, int lineHeight) {
        return Math.max(0, Math.min(skin.bottom(), height - skin.top() - lineHeight));
    }

    private static Identifier texture(String name) {
        return new Identifier(MOD_ID, "textures/gui/widget/" + name + ".png");
    }

    private static void skin(DrawContext graphics, Skin skin, int x, int y, int width, int height, int bottomInset) {
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
            graphics.drawTexture(texture, x, y + top, left, centerHeight, 0.0F, top, left, faceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            graphics.drawTexture(texture, rightX, y + top, right, centerHeight, TEXTURE_WIDTH - right, top, right, faceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        if (centerWidth > 0) {
            graphics.drawTexture(texture, x + left, y, centerWidth, top, left, 0.0F, faceWidth, top, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            graphics.drawTexture(texture, x + left, bottomY, centerWidth, bottom, left, bandTop, faceWidth, bottom, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        if (centerWidth > 0 && centerHeight > 0) {
            graphics.drawTexture(texture, x + left, y + top, centerWidth, centerHeight, left, top, faceWidth, faceHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        graphics.drawTexture(texture, x, y, left, top, 0.0F, 0.0F, left, top, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.drawTexture(texture, rightX, y, right, top, TEXTURE_WIDTH - right, 0.0F, right, top, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.drawTexture(texture, x, bottomY, left, bottom, 0.0F, bandTop, left, bottom, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        graphics.drawTexture(texture, rightX, bottomY, right, bottom, TEXTURE_WIDTH - right, bandTop, right, bottom, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private record Skin(Identifier texture, int left, int right, int top, int bottom) {
        int faceWidth() {
            return TEXTURE_WIDTH - left - right;
        }

        int faceHeight() {
            return TEXTURE_HEIGHT - top - bottom;
        }
    }
}