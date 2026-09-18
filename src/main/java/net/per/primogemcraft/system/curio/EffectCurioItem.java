package net.per.primogemcraft.system.curio;

import java.util.function.Consumer;

public class EffectCurioItem extends CurioItem {
    private final Consumer<CurioContext> burst;

    public EffectCurioItem(CurioForm form, int integrity, Consumer<CurioContext> burst, Properties properties) {
        super(CurioTrigger.RIGHT_CLICK, form, integrity, properties);
        this.burst = burst;
    }

    @Override
    public void activated(CurioContext context) {
        burst.accept(context);
    }
}
