package net.per.primogemcraft.collab.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.per.primogemcraft.client.gui.GuiAtlas;
import net.per.primogemcraft.registry.PGCItems;

public class WishMaterialCategory implements IRecipeCategory<WishMaterialRecipe> {
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 7;
    private final IDrawable icon;

    public WishMaterialCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(PGCItems.WISH_CORE.get());
    }

    @Override
    public RecipeType<WishMaterialRecipe> getRecipeType() {
        return PGCJEIPlugin.WISH_MATERIALS;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("rei.category.primogemcraft.wish_materials");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WishMaterialRecipe.COLUMNS * SLOT_SIZE + PADDING * 2;
    }

    @Override
    public int getHeight() {
        return WishMaterialRecipe.ROWS * SLOT_SIZE + PADDING * 2;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WishMaterialRecipe recipe, IFocusGroup focuses) {
        for (var index = 0; index < recipe.inputs().size(); index++) {
            var value = recipe.values().get(index);
            builder.addSlot(RecipeIngredientRole.INPUT, PADDING + index % WishMaterialRecipe.COLUMNS * SLOT_SIZE + 1,
                            PADDING + index / WishMaterialRecipe.COLUMNS * SLOT_SIZE + 1)
                    .addItemStack(recipe.inputs().get(index))
                    .addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.translatable("rei.primogemcraft.wish_materials.tooltip.0", value)
                            .withStyle(ChatFormatting.AQUA)));
        }
    }

    @Override
    public void draw(WishMaterialRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        GuiAtlas.panel(graphics, 0, 0, getWidth(), getHeight());
        for (var index = 0; index < WishMaterialRecipe.PAGE_SIZE; index++) {
            GuiAtlas.sprite(graphics, GuiAtlas.SLOT, PADDING + index % WishMaterialRecipe.COLUMNS * SLOT_SIZE,
                    PADDING + index / WishMaterialRecipe.COLUMNS * SLOT_SIZE);
        }
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }
}
