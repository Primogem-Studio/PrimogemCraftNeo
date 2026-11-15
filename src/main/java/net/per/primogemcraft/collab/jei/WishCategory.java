package net.per.primogemcraft.collab.jei;

import net.minecraft.client.render.DiffuseLighting;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.client.gui.GuiAtlas;
import net.per.primogemcraft.entity.misc.WishEntity;
import net.per.primogemcraft.registry.PGCEntities;
import net.per.primogemcraft.registry.PGCItems;
import org.joml.Quaternionf;

public class WishCategory implements IRecipeCategory<WishRecipe> {
    private final IDrawable icon;
    private final IDrawable arrow;
    private WishEntity entity;

    public WishCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(PGCItems.INTERTWINED_FATE.get());
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<WishRecipe> getRecipeType() {
        return PGCJEIPlugin.WISH;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("rei.category.primogemcraft.wish");
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
        return 36;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WishRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 10).addItemStack(recipe.output());
    }

    @Override
    public void draw(WishRecipe recipe, IRecipeSlotsView slots, DrawContext graphics, double mouseX, double mouseY) {
        GuiAtlas.panel(graphics, 0, 0, getWidth(), getHeight());
        GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 80, 9);
        arrow.draw(graphics, 48, 10);
        var client = MinecraftClient.getInstance();
        var level = client.world;
        if (level == null) return;
        if (entity == null || entity.getWorld() != level) entity = new WishEntity(PGCEntities.WISH_ENTITY.get(), level);
        entity.applyDisplay(recipe.rarity());
        entity.setAge((int) (net.minecraft.util.Util.getMeasuringTimeMs() / 50L));
        var pose = graphics.getMatrices();
        pose.push();
        pose.translate(20, 18, 50);
        pose.scale(30.0F, 30.0F, -30.0F);
        pose.multiply(new Quaternionf().rotateZ((float) Math.PI + MathHelper.RADIANS_PER_DEGREE * 25.0F).rotateX(-MathHelper.RADIANS_PER_DEGREE * 25.0F));
        pose.translate(0.0F, -entity.getHeight() / 2.0F, 0.0F);
        DiffuseLighting.enableGuiDepthLighting();
        client.getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, pose, graphics.getVertexConsumers(), net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
        graphics.draw();
        DiffuseLighting.enableGuiDepthLighting();
        pose.pop();
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }
}
