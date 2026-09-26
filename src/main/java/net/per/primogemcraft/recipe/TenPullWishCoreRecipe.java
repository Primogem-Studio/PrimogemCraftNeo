package net.per.primogemcraft.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.misc.WishCoreItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.system.wish.WishValue;

import java.util.List;
import java.util.Map;

public class TenPullWishCoreRecipe extends ShapedRecipe {
    public TenPullWishCoreRecipe() {
        super("", CraftingBookCategory.MISC,
                ShapedRecipePattern.of(Map.of('C', Ingredient.of(PGCItems.WISH_CORE.get(), PGCItems.UNBUFFED_WISH_CORE.get())), List.of("CCC", "CCC", "CCC")),
                new ItemStack(PGCItems.TEN_PULL_WISH_CORE.get()));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var result = super.assemble(input, registries);
        result.applyComponents(input.getItem(0).getComponentsPatch());
        var value = 0;
        var consumed = 0;
        for (var stack : input.items()) {
            value += WishValue.get(stack);
            var bar = stack.get(PGCDataComponents.CUSTOM_BAR);
            if (bar != null) consumed += bar.numerator();
        }
        result.set(PGCDataComponents.WISH_VALUE, value);
        result.set(PGCDataComponents.CUSTOM_BAR, new CustomBar(consumed, WishCoreItem.TEN_PULL_CAPACITY, true));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PGCRecipeSerializers.TEN_PULL_WISH_CORE.get();
    }

    public static class Serializer implements RecipeSerializer<TenPullWishCoreRecipe> {
        @Override
        public MapCodec<TenPullWishCoreRecipe> codec() {
            return MapCodec.unit(TenPullWishCoreRecipe::new);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TenPullWishCoreRecipe> streamCodec() {
            return StreamCodec.of((buffer, recipe) -> {}, buffer -> new TenPullWishCoreRecipe());
        }
    }
}
