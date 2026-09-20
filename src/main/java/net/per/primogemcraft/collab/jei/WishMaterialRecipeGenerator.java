package net.per.primogemcraft.collab.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.client.WishMaterialValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WishMaterialRecipeGenerator implements ISimpleRecipeManagerPlugin<WishMaterialRecipe> {
    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return input.getIngredient(VanillaTypes.ITEM_STACK)
                .map(stack -> WishMaterialValues.get().containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem())))
                .orElse(false);
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return false;
    }

    @Override
    public List<WishMaterialRecipe> getRecipesForInput(ITypedIngredient<?> input) {
        var stack = input.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) return List.of();
        var value = WishMaterialValues.get().get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (value == null) return List.of();
        return List.of(new WishMaterialRecipe(List.of(new ItemStack(stack.getItem())), List.of(value)));
    }

    @Override
    public List<WishMaterialRecipe> getRecipesForOutput(ITypedIngredient<?> output) {
        return List.of();
    }

    @Override
    public List<WishMaterialRecipe> getAllRecipes() {
        var entries = WishMaterialValues.get().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        var recipes = new ArrayList<WishMaterialRecipe>();
        var inputs = new ArrayList<ItemStack>();
        var values = new ArrayList<Integer>();
        for (var entry : entries) {
            var item = BuiltInRegistries.ITEM.getOptional(entry.getKey());
            if (item.isEmpty() || entry.getValue() <= 1) continue;
            inputs.add(new ItemStack(item.get()));
            values.add(entry.getValue());
            if (inputs.size() == WishMaterialRecipe.PAGE_SIZE) {
                recipes.add(new WishMaterialRecipe(inputs, values));
                inputs.clear();
                values.clear();
            }
        }
        if (!inputs.isEmpty()) recipes.add(new WishMaterialRecipe(inputs, values));
        return recipes;
    }
}
