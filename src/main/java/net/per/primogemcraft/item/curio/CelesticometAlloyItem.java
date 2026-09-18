package net.per.primogemcraft.item.curio;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.per.primogemcraft.system.curio.*;

public class CelesticometAlloyItem extends CurioItem {
    private static final String TYPE_I_KEY = "item.primogemcraft.celesticomet_alloy.type_i";
    private static final String TYPE_II_KEY = "item.primogemcraft.celesticomet_alloy.type_ii";

    public CelesticometAlloyItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var stack = context.stack();
        var mark = Curios.idOf(stack.getItem());
        if (Curios.marked(stack, mark) || context.player().isCreative()) return;
        Curios.mark(stack, mark);
        switch (Mth.nextInt(context.random(), 1, 3)) {
            case 2 -> stack.set(DataComponents.CUSTOM_NAME, Component.translatable(TYPE_I_KEY));
            case 3 -> stack.set(DataComponents.CUSTOM_NAME, Component.translatable(TYPE_II_KEY));
            default -> {
            }
        }
    }
}
