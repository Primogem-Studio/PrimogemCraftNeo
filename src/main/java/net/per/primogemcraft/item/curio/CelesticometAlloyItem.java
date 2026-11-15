package net.per.primogemcraft.item.curio;
import net.per.primogemcraft.compat.component.VanillaComponents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.system.curio.*;

public class CelesticometAlloyItem extends CurioItem {
    private static final String TYPE_I_KEY = "item.primogemcraft.celesticomet_alloy.type_i";
    private static final String TYPE_II_KEY = "item.primogemcraft.celesticomet_alloy.type_ii";

    public CelesticometAlloyItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var stack = context.stack();
        var mark = Curios.idOf(stack.getItem());
        if (Curios.marked(stack, mark) || context.player().isCreative()) return;
        Curios.mark(stack, mark);
        switch (MathHelper.nextInt(context.random(), 1, 3)) {
            case 2 -> VanillaComponents.setCustomName(stack, Text.translatable(TYPE_I_KEY));
            case 3 -> VanillaComponents.setCustomName(stack, Text.translatable(TYPE_II_KEY));
            default -> {
            }
        }
    }
}
