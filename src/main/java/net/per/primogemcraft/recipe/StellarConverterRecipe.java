package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.world.World;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.registry.PGCRecipeTypes;

/**
 * 星髓转换器配方（Yarn 1.20.1）。
 * 界面内双向转换，不走原版合成台匹配。
 */
public record StellarConverterRecipe(Identifier id, ItemStack first, ItemStack second, int cost)
        implements Recipe<Inventory> {

    public record Conversion(ItemStack input, ItemStack output, int cost) {
    }

    public Conversion match(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (matches(first, stack)) return new Conversion(first.copy(), second.copy(), cost);
        if (matches(second, stack)) return new Conversion(second.copy(), first.copy(), cost);
        return null;
    }

    @Override
    public boolean matches(Inventory input, World world) {
        return false;
    }

    @Override
    public ItemStack craft(Inventory input, DynamicRegistryManager registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registries) {
        return ItemStack.EMPTY;
    }

    public boolean isIgnoredInBook() {
        return true;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PGCRecipeSerializers.STELLAR_CONVERTER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PGCRecipeTypes.STELLAR_CONVERTER.get();
    }

    private static boolean matches(ItemStack expected, ItemStack stack) {
        return ItemStack.areItemsEqual(expected, stack) && java.util.Objects.equals(expected.getNbt(), stack.getNbt()) && stack.getCount() >= expected.getCount();
    }

    public static class Serializer implements RecipeSerializer<StellarConverterRecipe> {

        @Override
        public StellarConverterRecipe read(Identifier id, JsonObject json) {
            var first = readStack(JsonHelper.getObject(json, "first"));
            var second = readStack(JsonHelper.getObject(json, "second"));
            var cost = JsonHelper.getInt(json, "cost", 0);
            return new StellarConverterRecipe(id, first, second, cost);
        }

        @Override
        public StellarConverterRecipe read(Identifier id, PacketByteBuf buffer) {
            return new StellarConverterRecipe(id, buffer.readItemStack(), buffer.readItemStack(), buffer.readVarInt());
        }

        @Override
        public void write(PacketByteBuf buffer, StellarConverterRecipe recipe) {
            buffer.writeItemStack(recipe.first);
            buffer.writeItemStack(recipe.second);
            buffer.writeVarInt(recipe.cost);
        }

        private static ItemStack readStack(JsonObject json) {
            var item = net.minecraft.registry.Registries.ITEM.get(new Identifier(JsonHelper.getString(json, "item")));
            var count = JsonHelper.getInt(json, "count", 1);
            return new ItemStack(item, count);
        }
    }
}
