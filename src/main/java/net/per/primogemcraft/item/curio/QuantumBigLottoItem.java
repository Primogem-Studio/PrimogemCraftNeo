package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioReward;
import net.per.primogemcraft.system.curio.Curios;
import net.per.primogemcraft.system.curio.LotteryCurioItem;

import java.util.List;

public class QuantumBigLottoItem extends LotteryCurioItem {
    private static final int FRAGMENTS = 40;

    public QuantumBigLottoItem(Properties properties) {
        super(CurioForm.NORMAL, 0.7D, context -> {
            CurioReward.open(context.player(), ChoiceSupport.CURIO_CARDS, List.of(context.reward(Curios.randomCurio(context.random(), CurioForm.NEGATIVE.tag()))));
            context.announce(Component.translatable("message.primogemcraft.curio.lottery.negative_curio_gained"));
        }, context -> {
            context.give(new ItemStack(PGCItems.COSMIC_FRAGMENT.get(), FRAGMENTS));
            context.announce(Component.translatable("message.primogemcraft.curio.lottery.fragments_gained"));
        }, properties);
    }
}
