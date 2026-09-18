package net.per.primogemcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

/**
 * The shared sprite sheet every container window is drawn from. Elements sit on a fixed 32 pixel cell grid in
 * {@code textures/gui/container_window.png}. A slot sprite is exactly as large as a slot cell, so blitting it one pixel
 * before the slot position lines its interior up with the 16 pixel item that slot renders. The slot is bevelled the way
 * vanilla bevels one, and a hovered cell is highlighted with the vanilla translucent overlay instead of a second sprite.
 */
public final class GuiAtlas {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/container_window.png");
    public static final int WIDTH = 64;
    public static final int HEIGHT = 64;

    public static final Sprite PANEL = new Sprite(0, 0, 16, 16);
    public static final int PANEL_INSET = 2;
    public static final Sprite SLOT = new Sprite(32, 0, 18, 18);
    public static final Sprite SCROLL_TRACK = new Sprite(0, 32, 6, 20);
    public static final Sprite SCROLL_THUMB = new Sprite(32, 32, 6, 20);

    private GuiAtlas() {
    }

    public static void sprite(GuiGraphics graphics, Sprite sprite, int x, int y) {
        region(graphics, x, y, sprite.width(), sprite.height(), sprite.u(), sprite.v(), sprite.width(), sprite.height());
    }

    public static void stretched(GuiGraphics graphics, Sprite sprite, int x, int y, int width, int height) {
        region(graphics, x, y, width, height, sprite.u(), sprite.v(), sprite.width(), sprite.height());
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        var inset = Math.min(PANEL_INSET, Math.min(width, height) / 2);
        if (inset <= 0) {
            stretched(graphics, PANEL, x, y, width, height);
            return;
        }
        var sourceWidth = PANEL.width();
        var sourceHeight = PANEL.height();
        var faceWidth = sourceWidth - inset * 2;
        var faceHeight = sourceHeight - inset * 2;
        var right = x + width - inset;
        var bottom = y + height - inset;
        var centerWidth = width - inset * 2;
        var centerHeight = height - inset * 2;
        region(graphics, x, y, inset, inset, PANEL.u(), PANEL.v(), inset, inset);
        region(graphics, right, y, inset, inset, PANEL.u() + sourceWidth - inset, PANEL.v(), inset, inset);
        region(graphics, x, bottom, inset, inset, PANEL.u(), PANEL.v() + sourceHeight - inset, inset, inset);
        region(graphics, right, bottom, inset, inset, PANEL.u() + sourceWidth - inset, PANEL.v() + sourceHeight - inset, inset, inset);
        if (centerWidth > 0) {
            region(graphics, x + inset, y, centerWidth, inset, PANEL.u() + inset, PANEL.v(), faceWidth, inset);
            region(graphics, x + inset, bottom, centerWidth, inset, PANEL.u() + inset, PANEL.v() + sourceHeight - inset, faceWidth, inset);
        }
        if (centerHeight > 0) {
            region(graphics, x, y + inset, inset, centerHeight, PANEL.u(), PANEL.v() + inset, inset, faceHeight);
            region(graphics, right, y + inset, inset, centerHeight, PANEL.u() + sourceWidth - inset, PANEL.v() + inset, inset, faceHeight);
        }
        if (centerWidth > 0 && centerHeight > 0)
            region(graphics, x + inset, y + inset, centerWidth, centerHeight, PANEL.u() + inset, PANEL.v() + inset, faceWidth, faceHeight);
    }

    private static void region(GuiGraphics graphics, int x, int y, int width, int height, int u, int v, int sourceWidth, int sourceHeight) {
        if (width <= 0 || height <= 0) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, x, y, width, height, u, v, sourceWidth, sourceHeight, WIDTH, HEIGHT);
    }

    public record Sprite(int u, int v, int width, int height) {
    }
}
