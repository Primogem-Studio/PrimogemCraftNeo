package net.per.primogemcraft.item.curio;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.per.primogemcraft.registry.PGCEffects;
import net.per.primogemcraft.system.curio.AttributeCurioItem;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.effect.CurioEffects;

import java.util.List;

public class ThalanToxiFlameItem extends AttributeCurioItem {
    private static final int INTERVAL = 600;
    private static final double SPEED_BONUS = 0.05D;

    public ThalanToxiFlameItem(Settings properties) {
        super(CurioForm.NORMAL, List.of(new Modifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_BONUS, EntityAttributeModifier.Operation.ADDITION)), properties);
    }

    @Override
    public void presence(CurioContext context) {
        super.presence(context);
        CurioEffects.ensure(context.player(), PGCEffects.THALAN_TOXI_FLAME.get(), INTERVAL, 0);
    }
}
