package net.per.primogemcraft.item.misc;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemUsageContext;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCEntities;

public class XiaoLanternItem extends DescribedItem {
    private static final int COOLDOWN_TICKS = 10;

    public XiaoLanternItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var level = context.getWorld();
        var player = context.getPlayer();
        var above = context.getBlockPos().up();
        if (player == null || !level.getBlockState(above).isAir()) return ActionResult.PASS;
        if (level.isClient()) return ActionResult.SUCCESS;
        player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
        context.getStack().decrement(1);
        player.swingHand(context.getHand());
        if (level instanceof ServerWorld server) {
            var lantern = PGCEntities.XIAO_LANTERN.get().spawn(server, above, SpawnReason.MOB_SUMMONED);
            if (lantern != null) lantern.setYaw(level.getRandom().nextFloat() * 360.0F);
        }
        return ActionResult.SUCCESS;
    }
}
