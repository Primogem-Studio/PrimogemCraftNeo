package net.per.primogemcraft.item.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.per.primogemcraft.registry.PGCDataComponents;
import net.per.primogemcraft.system.curio.*;

public class VoidCheeseItem extends CurioItem {
    private static final double REPAIR_CHANCE = 0.2D;
    private static final double BREAK_CHANCE = 0.3D;
    private static final double HEAL_RATIO = 0.3D;
    private static final int REPAIR_AMOUNT = 1;

    public VoidCheeseItem(Properties properties) {
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
        var target = Curios.randomInventoryItem(player, VoidCheeseItem::worn);
        if (target.isEmpty()) return;
        if (!Curios.repair(player, target, REPAIR_AMOUNT)) return;
        if (!context.chance(BREAK_CHANCE)) return;
        context.destroy();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        var result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof ServerPlayer player)) return result;
        player.heal(player.getMaxHealth() * (float) HEAL_RATIO);
        Curios.broken(player, stack);
        return result;
    }

    private static boolean worn(ItemStack stack) {
        var bar = stack.get(PGCDataComponents.CUSTOM_BAR.get());
        return bar != null && bar.numerator() > 0 && !Curios.repairsCurios(stack);
    }
}
