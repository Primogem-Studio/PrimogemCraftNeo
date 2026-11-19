package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.per.primogemcraft.recipe.StellarConverterRecipe;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.registry.PGCRecipeTypes;

@REIPluginClient
public class PGCREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new WishCategory());
        registry.add(new ConversionCategory());
        registry.add(new WishMaterialCategory());
        registry.addWorkstations(PGCREIPlugin.WISH_MATERIALS, EntryStacks.of(PGCItems.WISH_CORE.get()),
                EntryStacks.of(PGCItems.TEN_PULL_WISH_CORE.get()));
        registry.addWorkstations(PGCREIPlugin.WISH, EntryStacks.of(PGCItems.ACQUAINT_FATE.get()), EntryStacks.of(PGCItems.INTERTWINED_FATE.get()));
        registry.addWorkstations(PGCREIPlugin.CONVERSION, EntryStacks.of(PGCItems.STELLAR_CONVERTER.get()));
    }

    @Override
    public void registerTransferHandlers(me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry registry) {
        registry.register(new WishCoreTransferHandler());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerDisplayGenerator(PGCREIPlugin.WISH_MATERIALS, new WishMaterialDisplayGenerator());
        registry.registerRecipeFiller(StellarConverterRecipe.class, PGCRecipeTypes.STELLAR_CONVERTER.get(), ConversionDisplay::new);
        registry.registerDisplayGenerator(PGCREIPlugin.WISH, new WishDisplayGenerator());
        var core = net.per.primogemcraft.system.wish.WishCoreAnvil.exampleCore();
        var fates = new net.minecraft.world.item.ItemStack(PGCItems.INTERTWINED_FATE.get(),
                net.per.primogemcraft.system.wish.WishCoreAnvil.FATE_COUNT);
        registry.add(new me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay(
                java.util.List.of(me.shedaniel.rei.api.common.util.EntryIngredients.of(fates),
                        me.shedaniel.rei.api.common.util.EntryIngredients.of(core)),
                java.util.List.of(me.shedaniel.rei.api.common.util.EntryIngredients.of(
                        net.per.primogemcraft.system.wish.WishCoreAnvil.assemble(fates, core))),
                java.util.Optional.empty(),
                java.util.OptionalInt.of(net.per.primogemcraft.system.wish.WishCoreAnvil.LEVEL_COST)));
    }
}
