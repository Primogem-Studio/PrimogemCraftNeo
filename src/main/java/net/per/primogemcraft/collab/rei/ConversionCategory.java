package net.per.primogemcraft.collab.rei;

import com.google.common.collect.ImmutableList;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.registry.PGCItems;
import org.joml.Matrix4f;

import java.util.List;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ConversionCategory implements DisplayCategory<ConversionDisplay> {
    private static final int DISPLAY_WIDTH = 120;
    private static final int DISPLAY_HEIGHT = 63;
    private static final int CHARGE_X = -50;
    private static final int CHARGE_Y = -12;
    private static final int CHARGE_WIDTH = 16;
    private static final int CHARGE_HEIGHT = 24;
    private static final int DUST_X = -56;
    private static final int DUST_Y = 20;
    private static final int DUST_SIZE = 8;
    private static final int DUST_LABEL_X = -43;
    private static final float DUST_LABEL_Y = 22.5F;
    private static final float DUST_LABEL_SCALE = 0.5F;
    private static final int LANE_INPUT_X = -30;
    private static final int LANE_ARROW_X = -9;
    private static final int LANE_OUTPUT_X = 20;
    private static final int FIRST_LANE_INPUT_Y = -22;
    private static final int FIRST_LANE_ARROW_Y = -23;
    private static final int SECOND_LANE_INPUT_Y = 8;
    private static final int SECOND_LANE_ARROW_Y = 7;
    private static final int DUST_COUNT_COLOR = 0xFF000000;

    private static final ResourceLocation CHARGE = texture();
    private static final ResourceLocation DUST = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/item/dust_of_azoth.png");
    private static final String TITLE_KEY = "rei.category.primogemcraft.conversion";
    private static final String COST_KEY = "rei.primogemcraft.tooltip.conversion_cost";

    @Override
    public List<Widget> setupDisplay(ConversionDisplay display, Rectangle bounds) {
        var center = new Point(bounds.getCenterX(), bounds.getCenterY());
        var widgets = ImmutableList.<Widget>builder();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createTexturedWidget(CHARGE, new Rectangle(center.x + CHARGE_X, center.y + CHARGE_Y, CHARGE_WIDTH, CHARGE_HEIGHT), 0.0F, 0.0F, CHARGE_WIDTH, CHARGE_HEIGHT));
        widgets.add(Widgets.createTooltip(new Rectangle(center.x + CHARGE_X, center.y + CHARGE_Y, CHARGE_WIDTH, CHARGE_HEIGHT), Component.translatable(COST_KEY, display.cost())));
        widgets.add(Widgets.createTexturedWidget(DUST, new Rectangle(center.x + DUST_X, center.y + DUST_Y, DUST_SIZE, DUST_SIZE), 0.0F, 0.0F, DUST_SIZE, DUST_SIZE));
        widgets.add(Widgets.withTranslate(Widgets.createLabel(new Point(0, 0), Component.literal("x" + display.dustCost())).color(DUST_COUNT_COLOR).noShadow(),
                new Matrix4f().translate(center.x + DUST_LABEL_X, center.y + DUST_LABEL_Y, 0.0F).scale(DUST_LABEL_SCALE, DUST_LABEL_SCALE, 1.0F)));
        addLane(widgets, center, display, FIRST_LANE_INPUT_Y, FIRST_LANE_ARROW_Y, 1, 0);
        addLane(widgets, center, display, SECOND_LANE_INPUT_Y, SECOND_LANE_ARROW_Y, 2, 1);
        return widgets.build();
    }

    @Override
    public Component getTitle() {
        return Component.translatable(TITLE_KEY);
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(PGCItems.STELLAR_CONVERTER.get());
    }

    @Override
    public int getDisplayHeight() {
        return DISPLAY_HEIGHT;
    }

    @Override
    public int getDisplayWidth(ConversionDisplay display) {
        return DISPLAY_WIDTH;
    }

    @Override
    public CategoryIdentifier<? extends ConversionDisplay> getCategoryIdentifier() {
        return PGCREIPlugin.CONVERSION;
    }

    private static void addLane(ImmutableList.Builder<Widget> widgets, Point center, ConversionDisplay display, int inputY, int arrowY, int inputIndex, int outputIndex) {
        widgets.add(Widgets.createSlot(new Point(center.x + LANE_INPUT_X, center.y + inputY)).entries(display.getInputEntries().get(inputIndex)).markInput());
        widgets.add(Widgets.createArrow(new Point(center.x + LANE_ARROW_X, center.y + arrowY)));
        widgets.add(Widgets.createSlot(new Point(center.x + LANE_OUTPUT_X, center.y + inputY)).entries(display.getOutputEntries().get(outputIndex)).markOutput());
    }

    private static ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/" + "stellar_converter_charge" + ".png");
    }
}
