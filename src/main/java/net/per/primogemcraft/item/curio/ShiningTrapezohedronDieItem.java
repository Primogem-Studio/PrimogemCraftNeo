package net.per.primogemcraft.item.curio;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.curio.*;

public class ShiningTrapezohedronDieItem extends CurioItem {
    public ShiningTrapezohedronDieItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void pickedUp(ServerPlayerEntity player, ItemStack stack) {
        trigger(player, stack);
    }

    @Override
    public void presence(CurioContext context) {
        trigger(context.player(), context.stack());
    }

    private void trigger(ServerPlayerEntity player, ItemStack stack) {
        var context = CurioContext.of(player, stack, form());
        var rewards = Curios.convert(context, 0.0D);
        context.destroy();
        CurioReward.open(player, ChoiceSupport.CURIO_CARDS, rewards);
    }
}
