package net.per.primogemcraft.item.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.*;

public class FictionalThreeEightDieItem extends CurioItem {
    private static final double BONUS_RATIO = 0.1D;

    public FictionalThreeEightDieItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
        trigger(player, stack);
    }

    @Override
    public void presence(CurioContext context) {
        trigger(context.player(), context.stack());
    }

    private void trigger(ServerPlayer player, ItemStack stack) {
        var context = CurioContext.of(player, stack, form());
        var rewards = Curios.convert(context, BONUS_RATIO);
        context.destroy();
        CurioReward.open(player, ChoiceSupport.CURIO_CARDS, rewards);
    }
}
