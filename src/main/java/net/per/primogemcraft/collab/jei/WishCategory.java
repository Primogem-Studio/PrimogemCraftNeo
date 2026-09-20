package net.per.primogemcraft.collab.jei;

import com.mojang.blaze3d.platform.Lighting;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
    public Component getTitle() {
        return Component.translatable("rei.category.primogemcraft.wish");
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
    public void draw(WishRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        GuiAtlas.panel(graphics, 0, 0, getWidth(), getHeight());
        GuiAtlas.sprite(graphics, GuiAtlas.SLOT, 80, 9);
        arrow.draw(graphics, 48, 10);
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        if (level == null) return;
        if (entity == null || entity.level() != level) entity = new WishEntity(PGCEntities.WISH_ENTITY.get(), level);
        entity.applyDisplay(recipe.rarity());
        entity.tickCount = (int) (Util.getMillis() / 50L);
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(20, 18, 50);
        pose.scale(30.0F, 30.0F, -30.0F);
        pose.mulPose(new Quaternionf().rotateZ((float) Math.PI + Mth.DEG_TO_RAD * 25.0F).rotateX(-Mth.DEG_TO_RAD * 25.0F));
        pose.translate(0.0F, -entity.getBbHeight() / 2.0F, 0.0F);
        Lighting.setupForEntityInInventory();
        minecraft.getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, pose, graphics.bufferSource(), LightTexture.FULL_BRIGHT);
        graphics.flush();
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }
}
