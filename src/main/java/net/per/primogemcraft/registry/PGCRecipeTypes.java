package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.recipe.StellarConverterRecipe;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_TYPE, MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<StellarConverterRecipe>> STELLAR_CONVERTER =
            REGISTRY.register("stellar_converter", () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(MOD_ID, "stellar_converter")));
}
