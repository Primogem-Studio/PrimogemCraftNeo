package net.per.primogemcraft.collab.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.WishCoreTransferPayload;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.wish.WishCoreTransfer;

import java.util.Optional;

public class WishCoreTransferHandler implements IRecipeTransferHandler<AnvilMenu, IJeiAnvilRecipe> {
    private final IRecipeTransferHandlerHelper helper;
    private final IRecipeTransferHandler<AnvilMenu, IJeiAnvilRecipe> fallback;

    public WishCoreTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
        fallback = helper.createUnregisteredRecipeTransferHandler(helper.createBasicRecipeTransferInfo(
                AnvilMenu.class, MenuType.ANVIL, RecipeTypes.ANVIL, 0, 2, 3, 36));
    }

    @Override
    public Class<AnvilMenu> getContainerClass() {
        return AnvilMenu.class;
    }

    @Override
    public Optional<MenuType<AnvilMenu>> getMenuType() {
        return Optional.of(MenuType.ANVIL);
    }

    @Override
    public RecipeType<IJeiAnvilRecipe> getRecipeType() {
        return RecipeTypes.ANVIL;
    }

    @Override
    public IRecipeTransferError transferRecipe(AnvilMenu menu, IJeiAnvilRecipe recipe, IRecipeSlotsView slots,
                                               Player player, boolean maxTransfer, boolean doTransfer) {
        if (recipe.getRightInputs().stream().noneMatch(stack -> stack.is(PGCItems.TEN_PULL_WISH_CORE.get())))
            return fallback.transferRecipe(menu, recipe, slots, player, maxTransfer, doTransfer);
        if (WishCoreTransfer.plan(menu).isEmpty())
            return helper.createUserErrorWithTooltip(Text.translatable("message.primogemcraft.wish.transfer_unavailable"));
        if (doTransfer) PayloadRegistrar.sendToServer(new WishCoreTransferPayload(menu.containerId));
        return null;
    }
}
