package net.per.primogemcraft.recipe;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCPotions;

public final class AbundanceElixirBrewing {
    private AbundanceElixirBrewing() {
    }

    public static void register() {
        FabricBrewingRecipeRegistry.registerPotionRecipe(Potions.THICK, Ingredient.ofItems(PGCItems.UNKNOWN_LEAF.get()), PGCPotions.ABUNDANCE_ELIXIR.get());
    }
}