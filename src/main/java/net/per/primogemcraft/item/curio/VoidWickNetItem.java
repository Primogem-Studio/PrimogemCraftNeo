package net.per.primogemcraft.item.curio;

import net.minecraft.resources.ResourceLocation;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.util.PlayerFlags;

import static net.per.primogemcraft.PrimogemCraft.MOD_ID;

public class VoidWickNetItem extends CurioItem {
    private static final ResourceLocation SEEN = ResourceLocation.fromNamespaceAndPath(MOD_ID, "curio/void_wick_net/seen");
    private static final int MAX_LEVEL = 5;
    private static final int BONUS_TICKS = 1200;

    public VoidWickNetItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var flags = PlayerFlags.of(player);
        var repairs = Curios.repairs(player);
        var seen = flags.counter(SEEN);
        if (repairs <= seen) return;
        flags.set(SEEN, repairs);
        var gained = Math.min(repairs - seen, MAX_LEVEL);
        var current = player.getEffect(PGCEffects.PARASITE);
        var level = current == null ? 0 : current.getAmplifier() + 1;
        var ticks = (current == null ? 0 : Math.max(current.getDuration(), 0)) + gained * BONUS_TICKS;
        Parasitism.apply(player, Math.min(level + gained, MAX_LEVEL), ticks);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.DEATH) return;
        context.destroy();
    }
}
