package net.per.primogemcraft.system.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RepairCurioItem extends CurioItem {
    private static final float FULL_REPAIR = 1.0F;

    public RepairCurioItem(CurioTrigger trigger, CurioForm form, int integrity, Properties properties) {
        super(trigger, form, integrity, properties);
    }

    @Override
    public boolean repairsCurios() {
        return true;
    }

    @Override
    public void pickedUp(ServerPlayer player, ItemStack stack) {
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
