package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.per.primogemcraft.registry.PGCItems;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

@EventBusSubscriber(modid = MOD_ID)
public final class OtherworldBankbookEvents {
    private OtherworldBankbookEvents() {
    }

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        var dropped = event.getItemEntity().getItem();
        if (!dropped.is(PGCItems.COSMIC_FRAGMENT.get())) return;
        OtherworldBankbook.absorbFragments(player, dropped);
    }
}
