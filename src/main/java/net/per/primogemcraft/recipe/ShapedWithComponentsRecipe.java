package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.per.primogemcraft.compat.component.StackComponents;
import net.per.primogemcraft.component.CustomBar;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.registry.PGCRecipeSerializers;

/**
 * 携带组件的有序合成配方。
 *
 * <p>1.21 用 {@code ShapedRecipePattern} 承载图案、{@code CraftingInput} 作输入，
 * 并以 {@code MapCodec}/{@code StreamCodec} 序列化。1.20.1 没有这些类型：
 * 这里直接继承 {@link ShapedRecipe}（自带宽高 + 原料列表与原版图案匹配），
 * 序列化改为 {@code fromJson}/{@code fromNetwork}/{@code toNetwork}。
 *
 * <p>组装时把来源物品的自定义进度条按比例迁移到产物，语义与原版一致。
 * 1.21 的 {@code applyComponents(item.getComponentsPatch())} 用于整体搬运组件；
 * 1.20.1 的组件数据都在 NBT 中，改为合并来源物品的 NBT。
 */
public class ShapedWithComponentsRecipe extends ShapedRecipe {
    private final Ingredient source;
    private final ItemStack result;

    public ShapedWithComponentsRecipe(Identifier id, String group, net.minecraft.recipe.book.CraftingRecipeCategory category,
                                      int width, int height, DefaultedList<Ingredient> ingredients,
                                      ItemStack result, Ingredient source, boolean showNotification) {
        super(id, group, category, width, height, ingredients, result, showNotification);
        this.source = source;
        this.result = result;
    }

    @Override
    public ItemStack craft(RecipeInputInventory input, DynamicRegistryManager registries) {
        var assembled = super.craft(input, registries);
        var target = StackComponents.get(assembled, PGCDataComponents.CUSTOM_BAR.get());
        for (var index = 0; index < input.size(); index++) {
            var item = input.getStack(index);
            if (!source.test(item)) continue;
            var carried = carriedBar(StackComponents.get(item, PGCDataComponents.CUSTOM_BAR.get()), target);
            // 1.20.1：组件数据落在 NBT，合并来源 NBT 等价于 1.21 的 applyComponents
            var itemTag = item.getNbt();
            if (itemTag != null) assembled.getOrCreateNbt().copyFrom(itemTag);
            if (carried != null) StackComponents.set(assembled, PGCDataComponents.CUSTOM_BAR.get(), carried);
            break;
        }
        return assembled;
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

        @Override
        public ShapedWithComponentsRecipe read(Identifier id, JsonObject json) {
            var group = JsonHelper.getString(json, "group", "");
            var category = net.minecraft.recipe.book.CraftingRecipeCategory.CODEC.byId(
                    JsonHelper.getString(json, "category", "misc"),
                    net.minecraft.recipe.book.CraftingRecipeCategory.MISC);
            var pattern = new ShapedRecipe.Serializer().read(id, json);
            var resultJson = JsonHelper.getObject(json, "result");
            var resultItem = net.minecraft.registry.Registries.ITEM.get(new Identifier(JsonHelper.getString(resultJson, "item")));
            var count = JsonHelper.getInt(resultJson, "count", 1);
            var result = new ItemStack(resultItem, count);
            var source = Ingredient.fromJson(json.get("source"));
            var showNotification = JsonHelper.getBoolean(json, "show_notification", true);
            return new ShapedWithComponentsRecipe(id, group, category,
                    pattern.getWidth(), pattern.getHeight(),
                    pattern.getIngredients(), result, source, showNotification);
        }

        @Override
        public ShapedWithComponentsRecipe read(Identifier id, PacketByteBuf buffer) {
            var width = buffer.readVarInt();
            var height = buffer.readVarInt();
            var group = buffer.readString();
            var category = buffer.readEnumConstant(net.minecraft.recipe.book.CraftingRecipeCategory.class);
            var ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromPacket(buffer));
            var result = buffer.readItemStack();
            var source = Ingredient.fromPacket(buffer);
            var showNotification = buffer.readBoolean();
            return new ShapedWithComponentsRecipe(id, group, category, width, height, ingredients, result, source, showNotification);
        }

        @Override
        public void write(PacketByteBuf buffer, ShapedWithComponentsRecipe recipe) {
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            for (var ingredient : recipe.getIngredients()) ingredient.write(buffer);
            buffer.writeItemStack(recipe.result);
            recipe.source.write(buffer);
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
