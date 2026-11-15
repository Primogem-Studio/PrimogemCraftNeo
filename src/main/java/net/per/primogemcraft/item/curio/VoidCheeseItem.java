package net.per.primogemcraft.item.curio;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioImpact;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioSignal;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.curio.Curios;

public class VoidCheeseItem extends CurioItem {
    private static final double REPAIR_CHANCE = 0.2D;
    private static final double BREAK_CHANCE = 0.3D;
    private static final double HEAL_RATIO = 0.3D;
    private static final int REPAIR_AMOUNT = 1;

    public VoidCheeseItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, 1, properties);
    }

    @Override
    public boolean repairsCurios() {
        return true;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.XP_PICKED) return;
        if (!context.chance(REPAIR_CHANCE)) return;
        var player = context.player();
        if (Curios.repairRandom(player, REPAIR_AMOUNT).isEmpty()) return;
        if (!context.chance(BREAK_CHANCE)) return;
        context.destroy();
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World level, LivingEntity entity) {
        var result = super.finishUsing(stack, level, entity);
        if (!(entity instanceof ServerPlayerEntity player)) return result;
        player.heal(player.getMaxHealth() * (float) HEAL_RATIO);
        Curios.broken(player, stack);
        return result;
    }

}
