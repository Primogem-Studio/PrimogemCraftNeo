package net.per.primogemcraft.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class ChoicePreviewRenderer {
    private static final float MODEL_UNITS = 16.0F;

    private ChoicePreviewRenderer() {
    }

    public static void renderItem(GuiGraphics graphics, ItemStack stack, float sizeUnits, float depthUnits) {
        if (stack.isEmpty()) return;

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, depthUnits);
        pose.scale(sizeUnits / MODEL_UNITS, sizeUnits / MODEL_UNITS, 1.0F);
        graphics.renderItem(stack, -8, -8);
        pose.popPose();
    }

    public static void renderTexture(GuiGraphics graphics, ResourceLocation texture, int boundsWidth, int boundsHeight) {
        if (texture == null) return;

        var size = TextureSizes.of(texture);
        if (size[0] <= 0 || size[1] <= 0) return;

        var fit = Math.min((float) boundsWidth / size[0], (float) boundsHeight / size[1]);
        var width = Math.max(1, Mth.floor(size[0] * fit));
        var height = Math.max(1, Mth.floor(size[1] * fit));
        graphics.blit(texture, -width / 2, -height / 2, width, height, 0.0F, 0.0F, size[0], size[1], size[0], size[1]);
    }
}
