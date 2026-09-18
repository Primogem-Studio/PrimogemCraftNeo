package net.per.primogemcraft.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class FamilyTiesItem extends CurioItem {
    public FamilyTiesItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        ExtraMagicDamage.strike(context, victim);
    }
}
