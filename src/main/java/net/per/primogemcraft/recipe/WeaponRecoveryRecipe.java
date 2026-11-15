package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
// removed forge: common.crafting.CraftingHelper;
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
 * 构造器为 {@code (Identifier, String, net.minecraft.recipe.book.CraftingRecipeCategory, ItemStack, NonNullList)}，
 * 序列化器用 {@code fromJson}/{@code fromNetwork}/{@code toNetwork}。
 *
 * <p>组装时把来源武器的状态写入产物的回收组件，语义与原版一致。
 */
public class WeaponRecoveryRecipe extends ShapelessRecipe {
    /** 1.20.1 的合成网格上限为 3x3。 */
    private static final int MAX_INGREDIENTS = 9;

    private final ItemStack result;

    public WeaponRecoveryRecipe(Identifier id, String group, net.minecraft.recipe.book.CraftingRecipeCategory category,
                                ItemStack result, DefaultedList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
        this.result = result;
    }

    @Override
    public ItemStack craft(RecipeInputInventory input, DynamicRegistryManager registries) {
        var assembled = super.craft(input, registries);
        for (var index = 0; index < input.size(); index++) {
            var stack = input.getStack(index);
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
        public WeaponRecoveryRecipe read(Identifier id, JsonObject json) {
            var group = JsonHelper.getString(json, "group", "");
            var category = net.minecraft.recipe.book.CraftingRecipeCategory.CODEC.byId(
                    JsonHelper.getString(json, "category", "misc"),
                    net.minecraft.recipe.book.CraftingRecipeCategory.MISC);
            var resultJson = JsonHelper.getObject(json, "result");
            var resultItem = net.minecraft.registry.Registries.ITEM.get(new Identifier(JsonHelper.getString(resultJson, "item")));
            var result = new ItemStack(resultItem, JsonHelper.getInt(resultJson, "count", 1));
            var ingredients = ingredientsFromJson(json);
            return new WeaponRecoveryRecipe(id, group, category, result, ingredients);
        }

        private static DefaultedList<Ingredient> ingredientsFromJson(JsonObject json) {
            var array = JsonHelper.getArray(json, "ingredients");
            var ingredients = DefaultedList.<Ingredient>of();
            for (var element : array) {
                var ingredient = Ingredient.fromJson(element);
                if (!ingredient.isEmpty()) ingredients.add(ingredient);
            }
            if (ingredients.isEmpty()) throw new com.google.gson.JsonSyntaxException("No ingredients for weapon recovery recipe");
            if (ingredients.size() > MAX_INGREDIENTS) throw new com.google.gson.JsonSyntaxException("Too many ingredients for weapon recovery recipe");
            return ingredients;
        }

        @Override
        public WeaponRecoveryRecipe read(Identifier id, PacketByteBuf buffer) {
            var group = buffer.readString();
            var category = buffer.readEnumConstant(net.minecraft.recipe.book.CraftingRecipeCategory.class);
            var size = buffer.readVarInt();
            var ingredients = DefaultedList.ofSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromPacket(buffer));
            var result = buffer.readItemStack();
            return new WeaponRecoveryRecipe(id, group, category, result, ingredients);
        }

        @Override
        public void write(PacketByteBuf buffer, WeaponRecoveryRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (var ingredient : recipe.getIngredients()) ingredient.write(buffer);
            buffer.writeItemStack(recipe.result);
        }
    }
}
