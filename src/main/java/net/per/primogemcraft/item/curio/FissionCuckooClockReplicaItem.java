package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;

public class FissionCuckooClockReplicaItem extends CurioItem {
    private static final String CLEARED_KEY = "message.primogemcraft.curio.fission_cuckoo_clock_replica.cleared";
    private static final String EMPTY_KEY = "message.primogemcraft.curio.fission_cuckoo_clock_replica.empty";
    private static final double HEALTH_COST = 0.35D;
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    public FissionCuckooClockReplicaItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public void presence(CurioContext context) {
        FissionCuckooClockItem.holdEffect(context.player());
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var stack = context.stack();
        var remaining = FissionCuckooClockItem.replicas(player) - 1;
        player.hurt(player.damageSources().generic(), (float) (player.getMaxHealth() * HEALTH_COST));
        FissionCuckooClockItem.resetSplit(player);
        if (remaining <= 0) {
            context.announce(Component.translatable(EMPTY_KEY));
        } else {
            context.announce(Component.translatable(CLEARED_KEY));
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundSource.PLAYERS, VOLUME, PITCH);
        stack.shrink(1);
        FissionCuckooClockItem.holdEffect(player);
    }
}
