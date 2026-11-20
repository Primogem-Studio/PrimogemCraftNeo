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
        super(id, "", CraftingRecipeCategory.MISC, 3, 3, ingredients(), new ItemStack(PGCItems.TEN_PULL_WISH_CORE.get()));
    }

    private static DefaultedList<Ingredient> ingredients() {
        var ingredient = Ingredient.ofItems(PGCItems.WISH_CORE.get(), PGCItems.UNBUFFED_WISH_CORE.get());
        var list = DefaultedList.<Ingredient>ofSize(9, Ingredient.EMPTY);
        for (var index = 0; index < 9; index++) list.set(index, ingredient);
        return list;
    }

    @Override
    public ItemStack craft(RecipeInputInventory input, DynamicRegistryManager registries) {
        var result = super.craft(input, registries);
        var first = input.getStack(0);
        var firstTag = first.getNbt();
        if (firstTag != null) result.getOrCreateNbt().copyFrom(firstTag);
        var value = 0;
        var consumed = 0;
        for (var index = 0; index < input.size(); index++) {
            var stack = input.getStack(index);
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
        public TenPullWishCoreRecipe read(Identifier id, JsonObject json) {
            return new TenPullWishCoreRecipe(id);
        }

        @Override
        public TenPullWishCoreRecipe read(Identifier id, PacketByteBuf buffer) {
            return new TenPullWishCoreRecipe(id);
        }

        @Override
        public void write(PacketByteBuf buffer, TenPullWishCoreRecipe recipe) {
        }
    }
}
