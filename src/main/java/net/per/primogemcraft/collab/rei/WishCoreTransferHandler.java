package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import net.minecraft.text.Text;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.compat.network.PayloadRegistrar;
import net.per.primogemcraft.network.WishCoreTransferPayload;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.wish.WishCoreTransfer;

public class WishCoreTransferHandler implements TransferHandler {
    @Override
    public double getPriority() {
        return 100;
    }

    @Override
    public Result handle(Context context) {
        if (!(context.getMenu() instanceof AnvilScreenHandler menu) || !(context.getDisplay() instanceof DefaultAnvilDisplay display)
                || display.getInputEntries().stream().flatMap(entry -> entry.stream()).noneMatch(entry ->
                entry.getType().equals(VanillaEntryTypes.ITEM) && entry.<ItemStack>castValue().isOf(PGCItems.TEN_PULL_WISH_CORE.get())))
            return Result.createNotApplicable();
        if (WishCoreTransfer.plan(menu).isEmpty())
            return Result.createFailed(Text.translatable("message.primogemcraft.wish.transfer_unavailable")).blocksFurtherHandling(false);
        if (context.isActuallyCrafting()) PayloadRegistrar.sendToServer(new WishCoreTransferPayload(menu.syncId));
        return Result.createSuccessful().blocksFurtherHandling(true);
    }
}
