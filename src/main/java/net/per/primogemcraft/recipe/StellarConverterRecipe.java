package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.registry.PGCRecipeTypes;

/**
 * 星髓转换器配方。
 *
 * <p>1.21 用 {@code Recipe<RecipeInput>} + {@code MapCodec}/{@code StreamCodec} 序列化；
 * 1.20.1 的 {@code Recipe} 以 {@link Container} 为输入，序列化器用
 * {@code fromJson}/{@code fromNetwork}/{@code toNetwork}。
 *
 * <p>本配方是「界面内双向转换」，不走原版合成台匹配，因此 {@code matches} 恒为 false，
 * 实际匹配由 {@link #match(ItemStack)} 提供，语义与原版一致。
 */
public record StellarConverterRecipe(ResourceLocation id, ItemStack first, ItemStack second, int cost)
        implements Recipe<Container> {

    public record Conversion(ItemStack input, ItemStack output, int cost) {
    }

    public Conversion match(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (matches(first, stack)) return new Conversion(first.copy(), second.copy(), cost);
        if (matches(second, stack)) return new Conversion(second.copy(), first.copy(), cost);
        return null;
    }

    @Override
    public boolean matches(Container input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container input, RegistryAccess registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
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
        return ItemStack.isSameItemSameTags(expected, stack) && stack.getCount() >= expected.getCount();
    }

    public static class Serializer implements RecipeSerializer<StellarConverterRecipe> {

        @Override
        public StellarConverterRecipe fromJson(ResourceLocation id, JsonObject json) {
            var first = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "first"), true);
            var second = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "second"), true);
            var cost = GsonHelper.getAsInt(json, "cost", 0);
            return new StellarConverterRecipe(id, first, second, cost);
        }

        @Override
        public StellarConverterRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new StellarConverterRecipe(id, buffer.readItem(), buffer.readItem(), buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, StellarConverterRecipe recipe) {
            buffer.writeItem(recipe.first);
            buffer.writeItem(recipe.second);
            buffer.writeVarInt(recipe.cost);
        }
    }
}
