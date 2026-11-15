package net.per.primogemcraft.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.system.menu.StellarConverterMenu;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class StellarConverterScreen extends HandledScreen<StellarConverterMenu> {
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 166;
    private static final int BUTTON_WIDTH = 18;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_PANEL_OVERLAP = 2;
    private static final int CHARGE_X = 7;
    private static final int CHARGE_Y = 9;
    private static final int CHARGE_WIDTH = 32;
    private static final int CHARGE_HEIGHT = 48;
    private static final int COVER_X = 11;
    private static final int COVER_Y = 13;
    private static final int COVER_WIDTH = 24;
    private static final int COVER_HEIGHT = 40;
    private static final int COVER_TEXTURE_WIDTH = 21;
    private static final int COVER_TEXTURE_HEIGHT = 1;

    private static final Identifier PANEL = texture("stellar_converter");
    private static final Identifier CHARGE = texture("stellar_converter_charge");
    private static final Identifier CHARGE_COVER = texture("stellar_converter_charge_cover");
    private static final String CHARGE_KEY = "gui.primogemcraft.stellar_converter.charge";

    public StellarConverterScreen(StellarConverterMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
        this.backgroundWidth = PANEL_WIDTH;
        this.backgroundHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        for (var channel = 0; channel < 2; channel++) {
            var channelIndex = channel;
            addDrawableChild(new ChannelButton(x + 132 + channel * 20,
                    y - BUTTON_HEIGHT + BUTTON_PANEL_OVERLAP, channelIndex));
        }
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        drawMouseoverTooltip(graphics, mouseX, mouseY);
        if (handler.getCursorStack().isEmpty() && hoveringCharge(mouseX, mouseY))
            graphics.drawTooltip(textRenderer,
                    Text.translatable(CHARGE_KEY, handler.charge(), StellarConverterBlockEntity.MAX_CHARGE), mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTick, int mouseX, int mouseY) {
        GuiAtlas.panel(graphics, x, y, backgroundWidth, backgroundHeight);
        for (var index = 0; index < handler.slots.size(); index++) {
            var slot = handler.slots.get(index);
            var sx = x + slot.x - 1;
            var sy = y + slot.y - 1;
            var matrices = graphics.getMatrices();
            matrices.push();
            if (index < StellarConverterMenu.SLOT_COUNT && handler.isSlotDisabled(index)) {
                matrices.translate(sx + GuiAtlas.SLOT.width(), sy + GuiAtlas.SLOT.height(), 0);
                matrices.scale(-1, -1, 1);
                GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 0, 0);
            } else {
                GuiAtlas.sprite(graphics, GuiAtlas.SLOT, sx, sy);
            }
            matrices.pop();
        }
        graphics.drawTexture(PANEL, x + 87, y + 22, 87, 22, 44, 18, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawTexture(PANEL, x + 87, y + 49, 87, 49, 44, 18, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawTexture(CHARGE, x + CHARGE_X, y + CHARGE_Y, CHARGE_WIDTH, CHARGE_HEIGHT,
                0.0F, 0.0F, CHARGE_WIDTH, CHARGE_HEIGHT, CHARGE_WIDTH, CHARGE_HEIGHT);
        var covered = Math.round(COVER_HEIGHT * MathHelper.clamp(
                1.0F - handler.charge() / (float) StellarConverterBlockEntity.MAX_CHARGE, 0.0F, 1.0F));
        if (covered > 0)
            graphics.drawTexture(CHARGE_COVER, x + COVER_X, y + COVER_Y, COVER_WIDTH, covered,
                    0.0F, 0.0F, COVER_TEXTURE_WIDTH, COVER_TEXTURE_HEIGHT, COVER_TEXTURE_WIDTH, COVER_TEXTURE_HEIGHT);
    }

    @Override
    protected void drawForeground(DrawContext graphics, int mouseX, int mouseY) {
    }

    private boolean hoveringCharge(int mouseX, int mouseY) {
        return mouseX >= x + CHARGE_X && mouseX < x + CHARGE_X + CHARGE_WIDTH
                && mouseY >= y + CHARGE_Y && mouseY < y + CHARGE_Y + CHARGE_HEIGHT;
    }

    private static Identifier texture(String name) {
        return new Identifier(MOD_ID, "textures/gui/" + name + ".png");
    }

    /** 用自定义九宫格皮肤绘制的通道开关按钮。 */
    private final class ChannelButton extends ButtonWidget {
        private final int channel;

        ChannelButton(int bx, int by, int channel) {
            super(bx, by, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Text.translatable("gui.primogemcraft.stellar_converter.channel_" + channel),
                    b -> {
                        if (client != null && client.interactionManager != null)
                            client.interactionManager.clickButton(handler.syncId, channel);
                    }, DEFAULT_NARRATION_SUPPLIER);
            this.channel = channel;
        }

        @Override
        protected void renderButton(DrawContext graphics, int mouseX, int mouseY, float delta) {
            NineSliceButton.draw(graphics, textRenderer, getX(), getY(), getWidth(), getHeight(), getMessage(),
                    active, isHovered(), handler.isChannelDisabled(channel));
        }
    }
}