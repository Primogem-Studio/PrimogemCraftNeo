package net.per.primogemcraft.item.curio;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioReward;
import net.per.primogemcraft.system.curio.Curios;

import java.util.ArrayList;

public class UnknownLostPropertyItem extends LostPropertyItem {
    private static final int DESCRIPTION_LINES = 3;
    private static final int MIN_CURIOS = 5;
    private static final int MAX_CURIOS = 10;

    public UnknownLostPropertyItem(Properties properties) {
        super(CurioForm.NORMAL, DESCRIPTION_LINES, properties);
    }

    @Override
    protected void grant(CurioContext context) {
        var rewards = new ArrayList<ItemStack>();
        for (var index = 0; index < Mth.nextInt(context.random(), MIN_CURIOS, MAX_CURIOS); index++) {
            var reward = Curios.randomCurio(context.random());
            if (!reward.isEmpty()) rewards.add(context.reward(reward));
        }
        CurioReward.open(context.player(), ChoiceSupport.CURIO_CARDS, rewards);
    }
}
