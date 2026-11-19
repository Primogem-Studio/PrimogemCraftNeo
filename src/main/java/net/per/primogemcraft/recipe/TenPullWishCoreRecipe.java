package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.misc.WishCoreItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.system.wish.WishValue;

public class TenPullWishCoreRecipe extends ShapedRecipe {
    public TenPullWishCoreRecipe(ResourceLocation id) {
        super(id, "", CraftingBookCategory.MISC, 3, 3, ingredients(), new ItemStack(PGCItems.TEN_PULL_WISH_CORE.get()));
    }

    private static NonNullList<Ingredient> ingredients() {
        var ingredient = Ingredient.of(PGCItems.WISH_CORE.get(), PGCItems.UNBUFFED_WISH_CORE.get());
        var list = NonNullList.<Ingredient>withSize(9, Ingredient.EMPTY);
        for (var index = 0; index < 9; index++) list.set(index, ingredient);
        return list;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        var result = super.assemble(input, registries);
        var first = input.getItem(0);
        var firstTag = first.getTag();
        if (firstTag != null) result.getOrCreateTag().merge(firstTag);
        var value = 0;
        var consumed = 0;
        for (var index = 0; index < input.getContainerSize(); index++) {
            var stack = input.getItem(index);
            value += WishValue.get(stack);
            var bar = StackComponents.get(stack, PGCDataComponents.CUSTOM_BAR);
            if (bar != null) consumed += bar.numerator();
        }
        StackComponents.set(result, PGCDataComponents.WISH_VALUE, value);
        StackComponents.set(result, PGCDataComponents.CUSTOM_BAR, new CustomBar(consumed, WishCoreItem.TEN_PULL_CAPACITY, true));
        StackComponents.set(result, PGCDataComponents.WISH_VALUE_DIVISOR, 10);
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PGCRecipeSerializers.TEN_PULL_WISH_CORE.get();
    }

    public static class Serializer implements RecipeSerializer<TenPullWishCoreRecipe> {
        @Override
        public TenPullWishCoreRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new TenPullWishCoreRecipe(id);
        }

        @Override
        public TenPullWishCoreRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new TenPullWishCoreRecipe(id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TenPullWishCoreRecipe recipe) {
        }
    }
}
