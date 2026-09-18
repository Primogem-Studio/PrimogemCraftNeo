package net.per.primogemcraft.system.curio;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class ContainerCurioItem extends CurioItem {
    private final int rolls;
    private final BiConsumer<CurioContext, CurioImpact> bonus;

    public ContainerCurioItem(CurioForm form, int integrity, int rolls, BiConsumer<CurioContext, CurioImpact> bonus, Properties properties) {
        super(CurioTrigger.ACTIVE, form, integrity, properties);
        this.rolls = rolls;
        this.bonus = bonus;
    }

    public static int jarProduction(ServerPlayer player) {
        var production = 1;
        for (var context : Curios.held(player))
            if (context.stack().getItem() instanceof ContainerCurioItem container) production += container.rolls;
        return production;
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.CONTAINER_BROKEN) return;
        var loot = CurioLoot.jarLoot(impact.state());
        for (var index = 0; index < rolls; index++)
            CurioLoot.dropTable(loot, impact.level(), impact.position(), impact.state(), context.player());
        if (bonus != null) bonus.accept(context, impact);
        context.damage(1);
    }
}
