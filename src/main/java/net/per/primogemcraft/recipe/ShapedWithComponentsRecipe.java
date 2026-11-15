package net.per.primogemcraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
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

    public ShapedWithComponentsRecipe(ResourceLocation id, String group, CraftingBookCategory category,
                                      int width, int height, NonNullList<Ingredient> ingredients,
                                      ItemStack result, Ingredient source, boolean showNotification) {
        super(id, group, category, width, height, ingredients, result, showNotification);
        this.source = source;
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        var assembled = super.assemble(input, registries);
        var target = StackComponents.get(assembled, PGCDataComponents.CUSTOM_BAR.get());
        for (var index = 0; index < input.getContainerSize(); index++) {
            var item = input.getItem(index);
            if (!source.test(item)) continue;
            var carried = carriedBar(StackComponents.get(item, PGCDataComponents.CUSTOM_BAR.get()), target);
            // 1.20.1：组件数据落在 NBT，合并来源 NBT 等价于 1.21 的 applyComponents
            var itemTag = item.getTag();
            if (itemTag != null) assembled.getOrCreateTag().merge(itemTag);
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
        public ShapedWithComponentsRecipe fromJson(ResourceLocation id, JsonObject json) {
            var group = GsonHelper.getAsString(json, "group", "");
            var category = CraftingBookCategory.CODEC
                    .byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            var pattern = new ShapedRecipe.Serializer().fromJson(id, json);
            var result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            var source = Ingredient.fromJson(json.get("source"));
            var showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);
            return new ShapedWithComponentsRecipe(id, group, category,
                    pattern.getRecipeWidth(), pattern.getRecipeHeight(),
                    pattern.getIngredients(), result, source, showNotification);
        }

        @Override
        public ShapedWithComponentsRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var width = buffer.readVarInt();
            var height = buffer.readVarInt();
            var group = buffer.readUtf();
            var category = buffer.readEnum(CraftingBookCategory.class);
            var ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
            var result = buffer.readItem();
            var source = Ingredient.fromNetwork(buffer);
            var showNotification = buffer.readBoolean();
            return new ShapedWithComponentsRecipe(id, group, category, width, height, ingredients, result, source, showNotification);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedWithComponentsRecipe recipe) {
            buffer.writeVarInt(recipe.getRecipeWidth());
            buffer.writeVarInt(recipe.getRecipeHeight());
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            for (var ingredient : recipe.getIngredients()) ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            recipe.source.toNetwork(buffer);
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
