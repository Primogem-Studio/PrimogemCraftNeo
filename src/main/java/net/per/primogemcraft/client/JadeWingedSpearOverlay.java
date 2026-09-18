package net.per.primogemcraft.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;

final class JadeWingedSpearOverlay implements LayeredDraw.Layer {
    private static final String SEGMENT = "◆";
    private static final int COLOR = 0x67BEF8;
    private static final int BOTTOM_OFFSET = 39;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;
        var stack = player.getMainHandItem();
        if (!stack.is(PGCItems.PRIMORDIAL_JADE_WINGED_SPEAR.get())) return;
        var charges = WeaponCharge.of(stack);
        if (charges < 1) return;
        var font = minecraft.font;
        var text = SEGMENT.repeat(charges);
        graphics.drawString(font, text, (graphics.guiWidth() - font.width(text)) / 2, graphics.guiHeight() - BOTTOM_OFFSET, COLOR);
    }
}
