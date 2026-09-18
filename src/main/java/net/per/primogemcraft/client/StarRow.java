package net.per.primogemcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public final class StarRow {
    public static final ResourceLocation STAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/curio_star.png");
    public static final ResourceLocation FUSION_STAR = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/curio_star_fusion.png");

    private static final int TILE = 16;
    private static final int SIZE = 9;
    private static final int GAP = 1;
    private static final float SCALE = 0.75F;

    private StarRow() {
    }

    public static void render(GuiGraphics graphics, int x, int y, int normal, int fusion) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(SCALE, SCALE, 1.0F);
        var offset = 0;
        for (var index = 0; index < normal; index++, offset += SIZE + GAP) blit(graphics, STAR, offset);
        for (var index = 0; index < fusion; index++, offset += SIZE + GAP) blit(graphics, FUSION_STAR, offset);
        pose.popPose();
    }

    public static int width(int count) {
        return Math.round(count * (SIZE + GAP) * SCALE);
    }

    public static int height() {
        return Math.round(SIZE * SCALE);
    }

    private static void blit(GuiGraphics graphics, ResourceLocation texture, int x) {
        graphics.blit(texture, x, 0, SIZE, SIZE, 0.0F, 0.0F, TILE, TILE, TILE, TILE);
    }
}
