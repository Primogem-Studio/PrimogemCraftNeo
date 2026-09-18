package net.per.primogemcraft.item.misc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.per.primogemcraft.registry.PGCEntities;

public class DendroCoreSpawnEggItem extends Item {
    private static final int BASE_COLOR = 0xFF33FF00;
    private static final int DOT_COLOR = 0xFFCCFF66;

    public DendroCoreSpawnEggItem(Properties properties) {
        super(properties);
    }

    public static int baseColor() {
        return BASE_COLOR;
    }

    public static int dotColor() {
        return DOT_COLOR;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
        var pos = context.getClickedPos().relative(context.getClickedFace());
        var core = PGCEntities.DENDRO_CORE.get().spawn(serverLevel, pos, MobSpawnType.SPAWN_EGG);
        if (core == null) return InteractionResult.FAIL;
        core.setYRot(serverLevel.getRandom().nextFloat() * 360.0F);
        context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
