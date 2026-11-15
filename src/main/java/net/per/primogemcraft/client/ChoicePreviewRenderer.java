package net.per.primogemcraft.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.item.ItemStack;

public final class ChoicePreviewRenderer {
    private static final float MODEL_UNITS = 16.0F;

    private ChoicePreviewRenderer() {
    }

    public static void renderItem(DrawContext graphics, ItemStack stack, float sizeUnits, float depthUnits) {
        if (stack.isEmpty()) return;

        var pose = graphics.getMatrices();
        pose.push();
        pose.translate(0.0F, 0.0F, depthUnits);
        pose.scale(sizeUnits / MODEL_UNITS, sizeUnits / MODEL_UNITS, 1.0F);
        graphics.drawItem(stack, -8, -8);
        pose.pop();
    }

    public static void renderTexture(DrawContext graphics, Identifier texture, int boundsWidth, int boundsHeight) {
        if (texture == null) return;

        var size = new int[]{64, 64};
        if (size[0] <= 0 || size[1] <= 0) return;

        var fit = Math.min((float) boundsWidth / size[0], (float) boundsHeight / size[1]);
        var width = Math.max(1, MathHelper.floor(size[0] * fit));
        var height = Math.max(1, MathHelper.floor(size[1] * fit));
        graphics.drawTexture(texture, -width / 2, -height / 2, width, height, 0.0F, 0.0F, size[0], size[1], size[0], size[1]);
    }
}
