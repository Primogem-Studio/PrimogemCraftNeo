package net.per.primogemcraft.item.misc;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.per.primogemcraft.registry.PGCEntities;

public class XiaoLanternSpawnEggItem extends Item {
    public XiaoLanternSpawnEggItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getWorld() instanceof ServerWorld serverLevel)) return ActionResult.SUCCESS;
        var pos = context.getBlockPos().offset(context.getSide());
        var lantern = PGCEntities.XIAO_LANTERN.get().spawn(serverLevel, pos, SpawnReason.SPAWN_EGG);
        if (lantern == null) return ActionResult.FAIL;
        lantern.setYaw(serverLevel.getRandom().nextFloat() * 360.0F);
        context.getStack().decrement(1);
        return ActionResult.CONSUME;
    }
}
