package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
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
        if (!(context.getMenu() instanceof AnvilMenu menu) || !(context.getDisplay() instanceof DefaultAnvilDisplay display)
                || display.getInputEntries().stream().flatMap(entry -> entry.stream()).noneMatch(entry ->
                entry.getType().equals(VanillaEntryTypes.ITEM) && entry.<ItemStack>castValue().is(PGCItems.TEN_PULL_WISH_CORE.get())))
            return Result.createNotApplicable();
        if (WishCoreTransfer.plan(menu).isEmpty())
            return Result.createFailed(Component.translatable("message.primogemcraft.wish.transfer_unavailable")).blocksFurtherHandling(false);
        if (context.isActuallyCrafting()) PacketDistributor.sendToServer(new WishCoreTransferPayload(menu.containerId));
        return Result.createSuccessful().blocksFurtherHandling(true);
    }
}
