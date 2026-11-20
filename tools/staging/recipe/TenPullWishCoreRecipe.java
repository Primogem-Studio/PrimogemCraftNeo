package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.item.misc.WishCoreItem;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.system.wish.WishValue;

public class TenPullWishCoreRecipe extends ShapedRecipe {
    public TenPullWishCoreRecipe(Identifier id) {
        super(id, "", CraftingBookCategory.MISC, 3, 3, ingredients(), new ItemStack(PGCItems.TEN_PULL_WISH_CORE.get()));
    }

    private static DefaultedList<Ingredient> ingredients() {
        var ingredient = Ingredient.of(PGCItems.WISH_CORE.get(), PGCItems.UNBUFFED_WISH_CORE.get());
        var list = DefaultedList.<Ingredient>withSize(9, Ingredient.EMPTY);
        for (var index = 0; index < 9; index++) list.set(index, ingredient);
        return list;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        var result = super.assemble(input, registries);
        var first = input.getItem(0);
        var firstTag = first.getNbt();
        if (firstTag != null) result.getOrCreateNbt().merge(firstTag);
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
        public TenPullWishCoreRecipe fromJson(Identifier id, JsonObject json) {
            return new TenPullWishCoreRecipe(id);
        }

        @Override
        public TenPullWishCoreRecipe fromNetwork(Identifier id, PacketByteBuf buffer) {
            return new TenPullWishCoreRecipe(id);
        }

        @Override
        public void toNetwork(PacketByteBuf buffer, TenPullWishCoreRecipe recipe) {
        }
    }
}
