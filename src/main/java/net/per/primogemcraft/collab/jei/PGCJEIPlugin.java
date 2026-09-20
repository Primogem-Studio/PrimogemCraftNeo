package net.per.primogemcraft.collab.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeTypes;
import net.per.primogemcraft.system.wish.WishDrops;
import net.per.primogemcraft.system.wish.WishRarity;

import java.util.ArrayList;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@JeiPlugin
public class PGCJEIPlugin implements IModPlugin {
    public static final RecipeType<WishRecipe> WISH = RecipeType.create(MOD_ID, "wish", WishRecipe.class);
    public static final RecipeType<StellarConverterRecipe> CONVERSION = RecipeType.create(MOD_ID, "conversion", StellarConverterRecipe.class);
    public static final RecipeType<WishMaterialRecipe> WISH_MATERIALS = RecipeType.create(MOD_ID, "wish_materials", WishMaterialRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new WishCategory(gui), new ConversionCategory(gui), new WishMaterialCategory(gui));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(PGCItems.WISH_CORE.get()), WISH_MATERIALS);
        registration.addRecipeCatalyst(new ItemStack(PGCItems.ACQUAINT_FATE.get()), WISH);
        registration.addRecipeCatalyst(new ItemStack(PGCItems.INTERTWINED_FATE.get()), WISH);
        registration.addRecipeCatalyst(new ItemStack(PGCItems.STELLAR_CONVERTER.get()), CONVERSION);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            registration.addRecipes(CONVERSION, level.getRecipeManager().getAllRecipesFor(PGCRecipeTypes.STELLAR_CONVERTER.get())
                    .stream().map(holder -> holder.value()).toList());
        }
        var wishes = new ArrayList<WishRecipe>();
        for (var rarity : WishRarity.values()) {
            for (var item : WishDrops.of(rarity)) wishes.add(new WishRecipe(rarity, new ItemStack(item)));
        }
        registration.addRecipes(WISH, wishes);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(WISH_MATERIALS, new WishMaterialRecipeGenerator());
    }
}
