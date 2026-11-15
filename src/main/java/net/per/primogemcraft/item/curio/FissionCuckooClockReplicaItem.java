package net.per.primogemcraft.item.curio;

import net.minecraft.text.Text;
import net.minecraft.sound.SoundCategory;
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

    public FissionCuckooClockReplicaItem(Settings properties) {
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
        player.damage(player.getDamageSources().generic(), (float) (player.getMaxHealth() * HEALTH_COST));
        FissionCuckooClockItem.resetSplit(player);
        if (remaining <= 0) {
            context.announce(Text.translatable(EMPTY_KEY));
        } else {
            context.announce(Text.translatable(CLEARED_KEY));
        }
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.CURIO_BROKEN.get(), SoundCategory.PLAYERS, VOLUME, PITCH);
        stack.decrement(1);
        FissionCuckooClockItem.holdEffect(player);
    }
}
