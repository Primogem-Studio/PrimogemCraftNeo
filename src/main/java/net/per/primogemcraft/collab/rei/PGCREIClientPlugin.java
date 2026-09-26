package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeTypes;
import net.per.primogemcraft.system.wish.WishCoreAnvil;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@REIPluginClient
public class PGCREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WishCategory());
        registry.add(new ConversionCategory());
        registry.add(new WishMaterialCategory());
        registry.addWorkstations(PGCREIPlugin.WISH_MATERIALS, EntryStacks.of(PGCItems.WISH_CORE.get()), EntryStacks.of(PGCItems.TEN_PULL_WISH_CORE.get()));
        registry.addWorkstations(PGCREIPlugin.WISH, EntryStacks.of(PGCItems.ACQUAINT_FATE.get()), EntryStacks.of(PGCItems.INTERTWINED_FATE.get()));
        registry.addWorkstations(PGCREIPlugin.CONVERSION, EntryStacks.of(PGCItems.STELLAR_CONVERTER.get()));
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new WishCoreTransferHandler());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        var core = WishCoreAnvil.exampleCore();
        var fates = new ItemStack(PGCItems.INTERTWINED_FATE.get(), WishCoreAnvil.FATE_COUNT);
        registry.add(new DefaultAnvilDisplay(List.of(EntryIngredients.of(fates), EntryIngredients.of(core)),
                List.of(EntryIngredients.of(WishCoreAnvil.assemble(fates, core))), Optional.empty(), OptionalInt.of(WishCoreAnvil.LEVEL_COST)));
        registry.registerDisplayGenerator(PGCREIPlugin.WISH_MATERIALS, new WishMaterialDisplayGenerator());
        registry.registerRecipeFiller(StellarConverterRecipe.class, PGCRecipeTypes.STELLAR_CONVERTER.get(), ConversionDisplay::new);
        registry.registerDisplayGenerator(PGCREIPlugin.WISH, new WishDisplayGenerator());
    }
}
