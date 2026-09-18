package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.per.primogemcraft.registry.PGCEntities;

public class XiaoLanternSpawnEggItem extends Item {
    public XiaoLanternSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        var pos = context.getClickedPos().relative(context.getClickedFace());
        var lantern = PGCEntities.XIAO_LANTERN.get().spawn(serverLevel, pos, MobSpawnType.SPAWN_EGG);
        if (lantern == null) return InteractionResult.FAIL;
        lantern.setYRot(serverLevel.getRandom().nextFloat() * 360.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
