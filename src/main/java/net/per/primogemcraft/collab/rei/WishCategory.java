package net.per.primogemcraft.collab.rei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.Lighting;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import org.joml.Quaternionf;

import java.util.List;

public class WishCategory implements DisplayCategory<WishDisplay> {
    private static final int DISPLAY_WIDTH = 120;
    private static final int DISPLAY_HEIGHT = 36;
    private static final int ENTITY_CENTER_X = 20;
    private static final int ENTITY_CENTER_Y = 18;
    private static final float ENTITY_SCALE = 30.0F;
    private static final float ENTITY_DEPTH = 50.0F;
    private static final float ENTITY_TILT = Mth.DEG_TO_RAD * 25.0F;
    private static final float ENTITY_ROLL = Mth.DEG_TO_RAD * 25.0F;
    private static final long MILLIS_PER_TICK = 50L;
    private static final int ARROW_LEFT = 48;
    private static final int ARROW_TOP = 10;
    private static final int SLOT_LEFT = 80;
    private static final int SLOT_TOP = 9;

    @Override
    public CategoryIdentifier<? extends WishDisplay> getCategoryIdentifier() {
        return PGCREIPlugin.WISH;
    }

    @Override
    public List<Widget> setupDisplay(WishDisplay display, Rectangle bounds) {
        var entity = new WishEntity(PGCEntities.WISH_ENTITY.get(), Minecraft.getInstance().level);
        entity.applyDisplay(display.rarity());

        var widgets = ImmutableList.<Widget>builder();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> renderEntity(graphics, entity, bounds)));
        widgets.add(Widgets.createArrow(new Point(bounds.x + ARROW_LEFT, bounds.y + ARROW_TOP)));
        widgets.add(Widgets.createSlot(new Point(bounds.x + SLOT_LEFT, bounds.y + SLOT_TOP)).entries(display.getOutputEntries().getFirst()).markOutput());
        return widgets.build();
    }

    @Override
    public Component getTitle() {
        return Component.translatable("rei.category.primogemcraft.wish");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(PGCItems.INTERTWINED_FATE.get());
    }

    @Override
    public int getDisplayHeight() {
        return DISPLAY_HEIGHT;
    }

    @Override
    public int getDisplayWidth(WishDisplay display) {
        return DISPLAY_WIDTH;
    }

    private static void renderEntity(GuiGraphics graphics, WishEntity entity, Rectangle bounds) {
        entity.tickCount = (int) (Util.getMillis() / MILLIS_PER_TICK);

        var poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(bounds.x + ENTITY_CENTER_X, bounds.y + ENTITY_CENTER_Y, ENTITY_DEPTH);
        poseStack.scale(ENTITY_SCALE, ENTITY_SCALE, -ENTITY_SCALE);
        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.PI + ENTITY_ROLL).rotateX(-ENTITY_TILT));
        poseStack.translate(0.0F, -entity.getBbHeight() / 2.0F, 0.0F);

        Lighting.setupForEntityInInventory();
        Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, graphics.bufferSource(), LightTexture.FULL_BRIGHT);
        graphics.flush();
        Lighting.setupFor3DItems();

        poseStack.popPose();
    }
}
