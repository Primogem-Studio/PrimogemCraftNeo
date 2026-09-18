package net.per.primogemcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCRecipeSerializers;

public class ShapedWithComponentsRecipe implements CraftingRecipe {
    private final String group;
    private final CraftingBookCategory category;
    private final ShapedRecipePattern pattern;
    private final ItemStack result;
    private final Ingredient source;
    private final boolean showNotification;

    public ShapedWithComponentsRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, Ingredient source, boolean showNotification) {
        this.group = group;
        this.category = category;
        this.pattern = pattern;
        this.result = result;
        this.source = source;
        this.showNotification = showNotification;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return pattern.matches(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var assembled = getResultItem(registries).copy();
        var target = assembled.get(PGCDataComponents.CUSTOM_BAR.get());
        for (var item : input.items()) {
            if (!source.test(item)) continue;
            var carried = carriedBar(item.get(PGCDataComponents.CUSTOM_BAR.get()), target);
            assembled.applyComponents(item.getComponentsPatch());
            if (carried != null) assembled.set(PGCDataComponents.CUSTOM_BAR.get(), carried);
            break;
        }
        return assembled;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= pattern.width() && height >= pattern.height();
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return pattern.ingredients();
    }

    @Override
    public boolean isIncomplete() {
        var ingredients = getIngredients();
        return ingredients.isEmpty() || ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(Ingredient::hasNoItems);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PGCRecipeSerializers.SHAPED_WITH_COMPONENTS.get();
    }

    private static CustomBar carriedBar(CustomBar stored, CustomBar target) {
        if (stored == null || target == null) return null;
        var numerator = Math.min(stored.numerator(), target.denominator());
        return new CustomBar(numerator, target.denominator(), numerator < target.denominator());
    }

    public static class Serializer implements RecipeSerializer<ShapedWithComponentsRecipe> {
        private static final MapCodec<ShapedWithComponentsRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Ingredient.CODEC.fieldOf("source").forGetter(recipe -> recipe.source),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(recipe -> recipe.showNotification)
        ).apply(instance, ShapedWithComponentsRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, ShapedWithComponentsRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::encode, Serializer::decode);

        @Override
        public MapCodec<ShapedWithComponentsRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedWithComponentsRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static void encode(RegistryFriendlyByteBuf buffer, ShapedWithComponentsRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.source);
            buffer.writeBoolean(recipe.showNotification);
        }

        private static ShapedWithComponentsRecipe decode(RegistryFriendlyByteBuf buffer) {
            var group = buffer.readUtf();
            var category = buffer.readEnum(CraftingBookCategory.class);
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            var result = ItemStack.STREAM_CODEC.decode(buffer);
            var source = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            return new ShapedWithComponentsRecipe(group, category, pattern, result, source, buffer.readBoolean());
        }
    }
}
