package net.per.primogemcraft.registry;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.recipe.RecipeType;
import net.per.primogemcraft.compat.registries.DeferredHolder;
import net.per.primogemcraft.compat.registries.DeferredRegister;
import net.per.primogemcraft.recipe.StellarConverterRecipe;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class PGCRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTRY = DeferredRegister.create(RegistryKeys.RECIPE_TYPE, MOD_ID);

    /**
     * 1.20.1 的 RecipeType 是纯标记接口。
     *
     * <p>不能用 {@code RecipeType.register(String)}：它会自行注册到 {@code minecraft:<name>}，
     * 与外层 DeferredRegister 的 {@code primogemcraft:<name>} 构成重复注册并在启动时崩溃。
     * 这里只创建实例，注册交给 DeferredRegister 完成。
     */
    public static final DeferredHolder<RecipeType<?>, RecipeType<StellarConverterRecipe>> STELLAR_CONVERTER =
            REGISTRY.register("stellar_converter", () -> new RecipeType<StellarConverterRecipe>() {
                @Override
                public String toString() {
                    return MOD_ID + ":stellar_converter";
                }
            });
}
