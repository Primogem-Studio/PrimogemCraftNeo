package net.per.primogemcraft.item.curio;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.per.primogemcraft.system.curio.AttributeCurioItem;
import net.per.primogemcraft.system.curio.CurioForm;

import java.util.List;

public class ClubFlameItem extends AttributeCurioItem {
    private static final double SPEED_BONUS = 0.02D;

    public ClubFlameItem(Properties properties) {
        super(CurioForm.FUSION, List.of(new Modifier(Attributes.MOVEMENT_SPEED, SPEED_BONUS, AttributeModifier.Operation.ADD_VALUE)), properties);
    }
}
