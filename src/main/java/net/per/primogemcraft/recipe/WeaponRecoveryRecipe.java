package net.per.primogemcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.per.primogemcraft.component.WeaponRecovery;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WishWeapon;

import java.util.List;

public class WeaponRecoveryRecipe extends ShapelessRecipe {
    private final ItemStack result;

    public WeaponRecoveryRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        var assembled = super.assemble(input, registries);
        for (var stack : input.items()) {
            if (!(stack.getItem() instanceof WishWeapon)) continue;
            assembled.set(PGCDataComponents.WEAPON_RECOVERY.get(), new WeaponRecovery(WeaponState.of(stack), WishWeapon.isFiveStar(stack)));
            break;
        }
        return assembled;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PGCRecipeSerializers.WEAPON_RECOVERY.get();
    }

    public static class Serializer implements RecipeSerializer<WeaponRecoveryRecipe> {
        private static final MapCodec<WeaponRecoveryRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(Serializer::toIngredients, DataResult::success).forGetter(ShapelessRecipe::getIngredients)
        ).apply(instance, WeaponRecoveryRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WeaponRecoveryRecipe> STREAM_CODEC = StreamCodec.of(Serializer::encode, Serializer::decode);

        @Override
        public MapCodec<WeaponRecoveryRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WeaponRecoveryRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static DataResult<NonNullList<Ingredient>> toIngredients(List<Ingredient> ingredients) {
            var array = ingredients.toArray(Ingredient[]::new);
            if (array.length == 0) return DataResult.error(() -> "No ingredients for weapon recovery recipe");
            return array.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()
                    ? DataResult.error(() -> "Too many ingredients for weapon recovery recipe")
                    : DataResult.success(NonNullList.of(Ingredient.EMPTY, array));
        }

        private static WeaponRecoveryRecipe decode(RegistryFriendlyByteBuf buffer) {
            var group = buffer.readUtf();
            var category = buffer.readEnum(CraftingBookCategory.class);
            var ingredients = NonNullList.withSize(buffer.readVarInt(), Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            return new WeaponRecoveryRecipe(group, category, ItemStack.STREAM_CODEC.decode(buffer), ingredients);
        }

        private static void encode(RegistryFriendlyByteBuf buffer, WeaponRecoveryRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (var ingredient : recipe.getIngredients()) Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }
}
