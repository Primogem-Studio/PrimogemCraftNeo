package net.per.primogemcraft.item.curio;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.per.primogemcraft.system.curio.AttributeCurioItem;
import net.per.primogemcraft.system.curio.CurioForm;

import java.util.List;

public class ClubFlameItem extends AttributeCurioItem {
    private static final double SPEED_BONUS = 0.02D;

    public ClubFlameItem(Settings properties) {
        super(CurioForm.FUSION, List.of(new Modifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, SPEED_BONUS, EntityAttributeModifier.Operation.ADDITION)), properties);
    }
}
