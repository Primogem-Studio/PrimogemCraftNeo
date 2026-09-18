package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.context.UseOnContext;
import net.per.primogemcraft.entity.misc.XiaoLanternEntity;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCEntities;

public class XiaoLanternItem extends DescribedItem {
    private static final int COOLDOWN_TICKS = 10;

    public XiaoLanternItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var above = context.getClickedPos().above();
        if (player == null || !level.getBlockState(above).isAir()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        context.getItemInHand().consume(1, player);
        player.swing(context.getHand(), true);
        if (level instanceof ServerLevel server && PGCEntities.XIAO_LANTERN.get().spawn(server, above, MobSpawnType.MOB_SUMMONED) instanceof XiaoLanternEntity lantern)
            lantern.setYRot(level.getRandom().nextFloat() * 360.0F);
        return InteractionResult.SUCCESS;
    }
}
