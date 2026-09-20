package net.per.primogemcraft.collab.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.client.gui.GuiAtlas;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class ConversionCategory implements IRecipeCategory<StellarConverterRecipe> {
    private static final ResourceLocation CHARGE = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/stellar_converter_charge.png");
    private final IDrawable icon;
    private final IDrawable arrow;

    public ConversionCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(PGCItems.STELLAR_CONVERTER.get());
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<StellarConverterRecipe> getRecipeType() {
        return PGCJEIPlugin.CONVERSION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("rei.category.primogemcraft.conversion");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 63;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StellarConverterRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 40)
                .addItemStack(new ItemStack(PGCItems.DUST_OF_AZOTH.get(), Mth.ceil(recipe.cost() / (double) StellarConverterBlockEntity.CHARGE_PER_DUST)));
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 10).addItemStack(recipe.first());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 10).addItemStack(recipe.second());
        builder.addSlot(RecipeIngredientRole.INPUT, 32, 40).addItemStack(recipe.second());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 82, 40).addItemStack(recipe.first());
    }

    @Override
    public void draw(StellarConverterRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        GuiAtlas.panel(graphics, 0, 0, getWidth(), getHeight());
        graphics.blit(CHARGE, 7, 9, 0, 0, 16, 24, 16, 24);
        GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 6, 39);
        for (var top : new int[]{9, 39}) {
            GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 31, top);
            GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 81, top);
            arrow.draw(graphics, 53, top);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, StellarConverterRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (mouseX >= 7 && mouseX < 23 && mouseY >= 9 && mouseY < 33) {
            tooltip.add(Component.translatable("rei.primogemcraft.tooltip.conversion_cost", recipe.cost()));
        }
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }
}
