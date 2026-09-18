package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.per.primogemcraft.block.entity.StellarConverterBlockEntity;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.registry.PGCItems;

import java.util.List;

public class ConversionDisplay implements Display {
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;
    private final int cost;

    public ConversionDisplay(RecipeHolder<StellarConverterRecipe> holder) {
        var recipe = holder.value();
        inputs = List.of(EntryIngredients.of(PGCItems.DUST_OF_AZOTH.get()), EntryIngredients.of(recipe.first()), EntryIngredients.of(recipe.second()));
        outputs = List.of(EntryIngredients.of(recipe.second()), EntryIngredients.of(recipe.first()));
        cost = recipe.cost();
    }

    public int cost() {
        return cost;
    }

    public int dustCost() {
        return Mth.ceil(cost / (double) StellarConverterBlockEntity.CHARGE_PER_DUST);
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return PGCREIPlugin.CONVERSION;
    }
}
