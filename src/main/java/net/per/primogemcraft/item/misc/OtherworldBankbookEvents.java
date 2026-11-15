package net.per.primogemcraft.item.misc;

import net.minecraft.server.network.ServerPlayerEntity;
import net.per.primogemcraft.compat.event.EventDispatch;
import net.per.primogemcraft.registry.PGCItems;

public final class OtherworldBankbookEvents {
    private OtherworldBankbookEvents() {
    }

    public static void register() {
        EventDispatch.onItemPickup((player, item) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
            var dropped = item.getStack();
            if (!dropped.isOf(PGCItems.COSMIC_FRAGMENT.get())) return;
            OtherworldBankbook.absorbFragments(serverPlayer, dropped);
        });
    }
}