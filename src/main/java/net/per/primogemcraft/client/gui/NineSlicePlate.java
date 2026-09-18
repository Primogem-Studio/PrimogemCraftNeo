package net.per.primogemcraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class NineSlicePlate {
    public static final int INSET_LEFT = 2;
    public static final int INSET_RIGHT = 2;
    public static final int INSET_TOP = 2;
    public static final int INSET_BOTTOM = 4;

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/widget/name_plate.png");
    private static final int SOURCE_W = 20;
    private static final int SOURCE_H = 25;
    private static final int FACE_W = SOURCE_W - INSET_LEFT - INSET_RIGHT;
    private static final int FACE_H = SOURCE_H - INSET_TOP - INSET_BOTTOM;
    private static final int BOTTOM_BAND_V = SOURCE_H - INSET_BOTTOM;

    private NineSlicePlate() {
    }

    public static void draw(GuiGraphics graphics, int x, int y, int width, int height, float pixelUnit) {
        var left = Math.round(INSET_LEFT * pixelUnit);
        var right = Math.round(INSET_RIGHT * pixelUnit);
        var top = Math.round(INSET_TOP * pixelUnit);
        var bottom = Math.round(INSET_BOTTOM * pixelUnit);
        var centerW = Math.max(0, width - left - right);
        var centerH = Math.max(0, height - top - bottom);
        var rightX = x + width - right;
        var bottomY = y + height - bottom;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (centerH > 0) {
            graphics.blit(TEXTURE, x, y + top, left, centerH, 0.0F, INSET_TOP, INSET_LEFT, FACE_H, SOURCE_W, SOURCE_H);
            graphics.blit(TEXTURE, rightX, y + top, right, centerH, SOURCE_W - INSET_RIGHT, INSET_TOP, INSET_RIGHT, FACE_H, SOURCE_W, SOURCE_H);
        }
        if (centerW > 0) {
            graphics.blit(TEXTURE, x + left, y, centerW, top, INSET_LEFT, 0.0F, FACE_W, INSET_TOP, SOURCE_W, SOURCE_H);
            graphics.blit(TEXTURE, x + left, bottomY, centerW, bottom, INSET_LEFT, BOTTOM_BAND_V, FACE_W, INSET_BOTTOM, SOURCE_W, SOURCE_H);
        }
        if (centerW > 0 && centerH > 0) {
            graphics.blit(TEXTURE, x + left, y + top, centerW, centerH, INSET_LEFT, INSET_TOP, FACE_W, FACE_H, SOURCE_W, SOURCE_H);
        }
        graphics.blit(TEXTURE, x, y, left, top, 0.0F, 0.0F, INSET_LEFT, INSET_TOP, SOURCE_W, SOURCE_H);
        graphics.blit(TEXTURE, rightX, y, right, top, SOURCE_W - INSET_RIGHT, 0.0F, INSET_RIGHT, INSET_TOP, SOURCE_W, SOURCE_H);
        graphics.blit(TEXTURE, x, bottomY, left, bottom, 0.0F, BOTTOM_BAND_V, INSET_LEFT, INSET_BOTTOM, SOURCE_W, SOURCE_H);
        graphics.blit(TEXTURE, rightX, bottomY, right, bottom, SOURCE_W - INSET_RIGHT, BOTTOM_BAND_V, INSET_RIGHT, INSET_BOTTOM, SOURCE_W, SOURCE_H);
    }
}
