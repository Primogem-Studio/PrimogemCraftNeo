package net.per.primogemcraft.system.curio;

import net.per.primogemcraft.registry.PGCDataComponents;

import java.util.function.Consumer;

public class SplittingCurioItem extends CurioItem {
    private final int interval;
    private final Consumer<CurioContext> burst;

    public SplittingCurioItem(CurioForm form, int integrity, int interval, Consumer<CurioContext> burst, Properties properties) {
        super(CurioTrigger.ACTIVE, form, integrity, properties);
        this.interval = interval;
        this.burst = burst;
    }

    @Override
    public int barCapacity() {
        return interval;
    }

    @Override
    public boolean barFillsUp() {
        return true;
    }

    @Override
    public void presence(CurioContext context) {
        var elapsed = context.progress() + 1;
        if (elapsed < interval) {
            context.progress(elapsed, interval);
            return;
        }
        context.progress(0, interval);
        burst.accept(context);
        spend(context);
    }

    private void spend(CurioContext context) {
        if (integrity() <= 0) return;
        var stack = context.stack();
        var charges = stack.getOrDefault(PGCDataComponents.CURIO_COUNTER.get(), integrity()) - 1;
        if (charges <= 0) {
            context.destroy();
            return;
        }
        stack.set(PGCDataComponents.CURIO_COUNTER.get(), charges);
    }
}
