package net.per.primogemcraft.item.curio;

import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.LotteryCurioItem;
import net.per.primogemcraft.system.event.EventQuota;
import net.per.primogemcraft.system.event.EventRegistry;

public class MysteryLotteryItem extends LotteryCurioItem {
    private static final double WIN_ODDS = 0.5D;

    public MysteryLotteryItem(Properties properties) {
        super(CurioForm.FUSION, WIN_ODDS, MysteryLotteryItem::grantEvent, MysteryLotteryItem::clearQuota, properties);
    }

    private static void grantEvent(CurioContext context) {
        EventRegistry.trigger(context.player(), EventRegistry.weightedGroup(context.random()));
    }

    private static void clearQuota(CurioContext context) {
        var player = context.player();
        EventQuota.addPlayerStored(player, -EventQuota.playerStored(player));
    }
}
