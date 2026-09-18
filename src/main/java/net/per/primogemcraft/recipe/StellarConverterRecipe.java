package net.per.primogemcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.registry.PGCRecipeTypes;

public record StellarConverterRecipe(ItemStack first, ItemStack second, int cost) implements Recipe<RecipeInput> {
    public record Conversion(ItemStack input, ItemStack output, int cost) {
    }

    public Conversion match(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (matches(first, stack)) return new Conversion(first.copy(), second.copy(), cost);
        if (matches(second, stack)) return new Conversion(second.copy(), first.copy(), cost);
        return null;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
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
        return ItemStack.isSameItemSameComponents(expected, stack) && stack.getCount() >= expected.getCount();
    }

    public static class Serializer implements RecipeSerializer<StellarConverterRecipe> {
        private static final MapCodec<StellarConverterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("first").forGetter(StellarConverterRecipe::first),
                ItemStack.STRICT_CODEC.fieldOf("second").forGetter(StellarConverterRecipe::second),
                Codec.INT.fieldOf("cost").forGetter(StellarConverterRecipe::cost)
        ).apply(instance, StellarConverterRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, StellarConverterRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::encode, Serializer::decode);

        @Override
        public MapCodec<StellarConverterRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StellarConverterRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static StellarConverterRecipe decode(RegistryFriendlyByteBuf buffer) {
            return new StellarConverterRecipe(ItemStack.STREAM_CODEC.decode(buffer), ItemStack.STREAM_CODEC.decode(buffer), buffer.readInt());
        }

        private static void encode(RegistryFriendlyByteBuf buffer, StellarConverterRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buffer, recipe.first);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.second);
            buffer.writeInt(recipe.cost);
        }
    }
}
