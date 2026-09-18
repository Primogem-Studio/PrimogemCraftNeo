package net.per.primogemcraft.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.per.primogemcraft.recipe.ShapedWithComponentsRecipe;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.recipe.WeaponRecoveryRecipe;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WeaponRecoveryRecipe>> WEAPON_RECOVERY =
            REGISTRY.register("weapon_recovery", WeaponRecoveryRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StellarConverterRecipe>> STELLAR_CONVERTER =
            REGISTRY.register("stellar_converter", StellarConverterRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedWithComponentsRecipe>> SHAPED_WITH_COMPONENTS =
            REGISTRY.register("crafting_shaped_with_components", ShapedWithComponentsRecipe.Serializer::new);
}
