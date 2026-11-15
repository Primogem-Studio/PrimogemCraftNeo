package net.per.primogemcraft.collab.rei;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.DiffuseLighting;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
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
    private static final float ENTITY_TILT = MathHelper.RADIANS_PER_DEGREE * 25.0F;
    private static final float ENTITY_ROLL = MathHelper.RADIANS_PER_DEGREE * 25.0F;
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
        var entity = new WishEntity(PGCEntities.WISH_ENTITY.get(), MinecraftClient.getInstance().world);
        entity.applyDisplay(display.rarity());

        var widgets = ImmutableList.<Widget>builder();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> renderEntity(graphics, entity, bounds)));
        widgets.add(Widgets.createArrow(new Point(bounds.x + ARROW_LEFT, bounds.y + ARROW_TOP)));
        widgets.add(Widgets.createSlot(new Point(bounds.x + SLOT_LEFT, bounds.y + SLOT_TOP)).entries(display.getOutputEntries().get(0)).markOutput());
        return widgets.build();
    }

    @Override
    public Text getTitle() {
        return Text.translatable("rei.category.primogemcraft.wish");
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

    private static void renderEntity(DrawContext graphics, WishEntity entity, Rectangle bounds) {
        entity.setAge((int) (net.minecraft.util.Util.getMeasuringTimeMs() / MILLIS_PER_TICK));

        var poseStack = graphics.getMatrices();
        poseStack.push();
        poseStack.translate(bounds.x + ENTITY_CENTER_X, bounds.y + ENTITY_CENTER_Y, ENTITY_DEPTH);
        poseStack.scale(ENTITY_SCALE, ENTITY_SCALE, -ENTITY_SCALE);
        poseStack.multiply(new Quaternionf().rotateZ((float) Math.PI + ENTITY_ROLL).rotateX(-ENTITY_TILT));
        poseStack.translate(0.0F, -entity.getHeight() / 2.0F, 0.0F);

        DiffuseLighting.enableGuiDepthLighting();
        MinecraftClient.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, poseStack, graphics.getVertexConsumers(), net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
        graphics.draw();
        DiffuseLighting.enableGuiDepthLighting();

        poseStack.pop();
    }
}
