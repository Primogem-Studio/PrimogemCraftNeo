package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.util.PlayerFlags;

import java.util.List;

public class CosmicBigLottoItem extends LotteryCurioItem {
    private static final String QUERY_COOLDOWN = "curio/lottery_query";
    private static final int QUERY_COOLDOWN_TICKS = 1200;
    private static final float FOOD_RETAINED = 0.02F;

    public CosmicBigLottoItem(Properties properties) {
        super(CurioForm.NORMAL, 0.4D, context -> {
            CurioReward.open(context.player(), ChoiceSupport.CURIO_CARDS, List.of(context.reward(Curios.randomCurio(context.random()))));
            context.announce(Component.translatable("message.primogemcraft.curio.lottery.curio_gained"));
        }, context -> {
            context.player().setHealth(1.0F);
            var data = context.player().getFoodData();
            data.setFoodLevel((int) (data.getFoodLevel() * FOOD_RETAINED));
        }, properties);
    }

    @Override
    public void activated(CurioContext context) {
        var breaks = PlayerFlags.of(context.player()).counter(LotteryCurioItem.BREAKS);
        if (!context.ready(QUERY_COOLDOWN, QUERY_COOLDOWN_TICKS)) {
            context.announce(Component.translatable("message.primogemcraft.curio.lottery.query", breaks));
            return;
        }
        var player = context.player();
        player.server.getPlayerList().broadcastSystemMessage(Component.translatable("message.primogemcraft.curio.lottery.query_broadcast", player.getDisplayName(), breaks), false);
    }
}
