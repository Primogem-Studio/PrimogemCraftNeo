package net.per.primogemcraft.collab.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.client.WishLootItems;
import net.per.primogemcraft.system.wish.WishRarity;

import java.util.ArrayList;
import java.util.List;

public class WishRecipeGenerator implements ISimpleRecipeManagerPlugin<WishRecipe> {
    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return false;
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return !getRecipesForOutput(output).isEmpty();
    }

    @Override
    public List<WishRecipe> getRecipesForInput(ITypedIngredient<?> input) {
        return List.of();
    }

    @Override
    public List<WishRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
        var stack = output.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) return List.of();
        var recipes = new ArrayList<WishRecipe>();
        for (var rarity : WishRarity.values()) {
            if (WishLootItems.of(rarity).contains(stack.getItem())) {
                recipes.add(new WishRecipe(rarity, new ItemStack(stack.getItem())));
            }
        }
        return recipes;
    }

    @Override
    public List<WishRecipe> getAllRecipes() {
        var recipes = new ArrayList<WishRecipe>();
        for (var rarity : WishRarity.values()) {
            for (var item : WishLootItems.of(rarity)) recipes.add(new WishRecipe(rarity, new ItemStack(item)));
        }
        return recipes;
    }
}
