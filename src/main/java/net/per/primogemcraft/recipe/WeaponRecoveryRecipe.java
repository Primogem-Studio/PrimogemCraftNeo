package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.component.WeaponRecovery;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCRecipeSerializers;
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WishWeapon;

/**
 * 武器回收配方（无序合成）。
 *
 * <p>1.21 用 {@code CraftingInput} + {@code MapCodec}/{@code StreamCodec}；
 * 1.20.1 的 {@code ShapelessRecipe} 以 {@link CraftingContainer} 为输入，
 * 构造器为 {@code (ResourceLocation, String, CraftingBookCategory, ItemStack, NonNullList)}，
 * 序列化器用 {@code fromJson}/{@code fromNetwork}/{@code toNetwork}。
 *
 * <p>组装时把来源武器的状态写入产物的回收组件，语义与原版一致。
 */
public class WeaponRecoveryRecipe extends ShapelessRecipe {
    /** 1.20.1 的合成网格上限为 3x3。 */
    private static final int MAX_INGREDIENTS = 9;

    private final ItemStack result;

    public WeaponRecoveryRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        var assembled = super.assemble(input, registries);
        for (var index = 0; index < input.getContainerSize(); index++) {
            var stack = input.getItem(index);
            if (!(stack.getItem() instanceof WishWeapon)) continue;
            StackComponents.set(assembled, PGCDataComponents.WEAPON_RECOVERY.get(),
                    new WeaponRecovery(WeaponState.of(stack), WishWeapon.isFiveStar(stack)));
            break;
        }
        return assembled;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PGCRecipeSerializers.WEAPON_RECOVERY.get();
    }

    public static class Serializer implements RecipeSerializer<WeaponRecoveryRecipe> {

        @Override
        public WeaponRecoveryRecipe fromJson(ResourceLocation id, JsonObject json) {
            var group = GsonHelper.getAsString(json, "group", "");
            var category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            var result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            var ingredients = ingredientsFromJson(json);
            return new WeaponRecoveryRecipe(id, group, category, result, ingredients);
        }

        private static NonNullList<Ingredient> ingredientsFromJson(JsonObject json) {
            var array = GsonHelper.getAsJsonArray(json, "ingredients");
            var ingredients = NonNullList.<Ingredient>create();
            for (var element : array) {
                var ingredient = Ingredient.fromJson(element);
                if (!ingredient.isEmpty()) ingredients.add(ingredient);
            }
            if (ingredients.isEmpty()) throw new com.google.gson.JsonSyntaxException("No ingredients for weapon recovery recipe");
            if (ingredients.size() > MAX_INGREDIENTS) throw new com.google.gson.JsonSyntaxException("Too many ingredients for weapon recovery recipe");
            return ingredients;
        }

        @Override
        public WeaponRecoveryRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var group = buffer.readUtf();
            var category = buffer.readEnum(CraftingBookCategory.class);
            var size = buffer.readVarInt();
            var ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            var result = buffer.readItem();
            return new WeaponRecoveryRecipe(id, group, category, result, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WeaponRecoveryRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (var ingredient : recipe.getIngredients()) ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }
}
