package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioReward;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.curio.LotteryCurioItem;

import java.util.List;

public class FictionalMegaLottoItem extends LotteryCurioItem {
    public FictionalMegaLottoItem(Properties properties) {
        super(CurioForm.FUSION, 0.8D, context -> {
            CurioReward.open(context.player(), ChoiceSupport.CURIO_CARDS, List.of(context.reward(Curios.randomCurio(context.random(), CurioForm.NORMAL.tag()))));
            context.announce(Component.translatable("message.primogemcraft.curio.lottery.normal_curio_gained"));
        }, context -> context.player().setHealth(1.0F), properties);
    }
}
