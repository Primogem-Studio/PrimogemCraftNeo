package net.per.primogemcraft.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.system.menu.StellarConverterMenu;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class StellarConverterScreen extends AbstractContainerScreen<StellarConverterMenu> {
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

    private static final ResourceLocation PANEL = texture("stellar_converter");
    private static final ResourceLocation CHARGE = texture("stellar_converter_charge");
    private static final ResourceLocation CHARGE_COVER = texture("stellar_converter_charge_cover");
    private static final String CHARGE_KEY = "gui.primogemcraft.stellar_converter.charge";

    public StellarConverterScreen(StellarConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = PANEL_WIDTH;
        imageHeight = PANEL_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        for (var channel = 0; channel < 2; channel++) {
            var channelIndex = channel;
            addRenderableWidget(new Button(leftPos + 132 + channel * 20, topPos - BUTTON_HEIGHT + BUTTON_PANEL_OVERLAP,
                    BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable("gui.primogemcraft.stellar_converter.channel_" + channel),
                    button -> {
                        if (minecraft != null && minecraft.gameMode != null)
                            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, channelIndex);
                    }, narration -> narration.get()) {
                @Override
                protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                    NineSliceButton.draw(graphics, font, getX(), getY(), getWidth(), getHeight(), getMessage(),
                            active, isHoveredOrFocused(), menu.isChannelDisabled(channelIndex));
                }
            });
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (menu.getCarried().isEmpty() && hoveringCharge(mouseX, mouseY))
            graphics.renderTooltip(font, Component.translatable(CHARGE_KEY, menu.charge(), StellarConverterBlockEntity.MAX_CHARGE), mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GuiAtlas.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (var index = 0; index < menu.slots.size(); index++) {
            var slot = menu.slots.get(index);
            var x = leftPos + slot.x - 1;
            var y = topPos + slot.y - 1;
            graphics.pose().pushPose();
            if (index < StellarConverterMenu.SLOT_COUNT && menu.isSlotDisabled(index)) {
                graphics.pose().translate(x + GuiAtlas.SLOT.width(), y + GuiAtlas.SLOT.height(), 0);
                graphics.pose().scale(-1, -1, 1);
                GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 0, 0);
            } else {
                GuiAtlas.sprite(graphics, GuiAtlas.SLOT, x, y);
            }
            graphics.pose().popPose();
        }
        graphics.blit(PANEL, leftPos + 87, topPos + 22, 87, 22, 44, 18, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.blit(PANEL, leftPos + 87, topPos + 49, 87, 49, 44, 18, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.blit(CHARGE, leftPos + CHARGE_X, topPos + CHARGE_Y, CHARGE_WIDTH, CHARGE_HEIGHT, 0.0F, 0.0F, CHARGE_WIDTH, CHARGE_HEIGHT, CHARGE_WIDTH, CHARGE_HEIGHT);
        var covered = Math.round(COVER_HEIGHT * Mth.clamp(1.0F - menu.charge() / (float) StellarConverterBlockEntity.MAX_CHARGE, 0.0F, 1.0F));
        if (covered > 0)
            graphics.blit(CHARGE_COVER, leftPos + COVER_X, topPos + COVER_Y, COVER_WIDTH, covered, 0.0F, 0.0F, COVER_TEXTURE_WIDTH, COVER_TEXTURE_HEIGHT, COVER_TEXTURE_WIDTH, COVER_TEXTURE_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private boolean hoveringCharge(int mouseX, int mouseY) {
        return mouseX >= leftPos + CHARGE_X && mouseX < leftPos + CHARGE_X + CHARGE_WIDTH
                && mouseY >= topPos + CHARGE_Y && mouseY < topPos + CHARGE_Y + CHARGE_HEIGHT;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/" + name + ".png");
    }
}
