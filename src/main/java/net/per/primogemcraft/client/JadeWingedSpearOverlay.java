package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.per.primogemcraft.component.WeaponCharge;
import net.per.primogemcraft.registry.PGCItems;

/**
 * 和璞鸢充能层数的 HUD 显示。
 *
 * <p>1.21 用 {@code LayeredDraw.Layer} + {@code RegisterGuiLayersEvent}；
 * 1.20.1 改为 {@link IGuiOverlay} + {@code RegisterGuiOverlaysEvent}，
 * 由 {@code PGCClientEvents} 注册。绘制位置与配色与原版一致。
 */
final class JadeWingedSpearOverlay implements IGuiOverlay {
    private static final String SEGMENT = "◆";
    private static final int COLOR = 0x67BEF8;
    private static final int BOTTOM_OFFSET = 39;

    static final JadeWingedSpearOverlay INSTANCE = new JadeWingedSpearOverlay();

    private JadeWingedSpearOverlay() {
    }

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics,
                       float partialTick, int screenWidth, int screenHeight) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null) return;
        var stack = player.getMainHandItem();
        if (!stack.is(PGCItems.PRIMORDIAL_JADE_WINGED_SPEAR.get())) return;
        var charges = WeaponCharge.of(stack);
        if (charges < 1) return;
        var font = minecraft.font;
        var text = SEGMENT.repeat(charges);
        graphics.drawString(font, text, (screenWidth - font.width(text)) / 2, screenHeight - BOTTOM_OFFSET, COLOR);
    }
}
