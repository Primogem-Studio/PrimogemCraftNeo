package net.per.primogemcraft.system.curio;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class RepairCurioItem extends CurioItem {
    private static final float FULL_REPAIR = 1.0F;

    public RepairCurioItem(CurioTrigger trigger, CurioForm form, int integrity, Settings properties) {
        super(trigger, form, integrity, properties);
    }

    @Override
    public boolean repairsCurios() {
        return true;
    }

    @Override
    public void pickedUp(ServerPlayerEntity player, ItemStack stack) {
        repair(CurioContext.of(player, stack, form()));
    }

    @Override
    public void presence(CurioContext context) {
        repair(context);
    }

    private void repair(CurioContext context) {
        if (context.remaining() <= 0) return;
        if (Curios.repairRandom(context.player(), FULL_REPAIR, context.stack()).isEmpty()) return;
        context.damage(1);
    }
}
