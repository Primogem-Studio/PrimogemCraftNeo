package net.per.primogemcraft.item.curio;

import net.minecraft.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class RobeOfHarmonyItem extends CurioItem {
    public RobeOfHarmonyItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties.fireproof());
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        ExtraMagicDamage.strike(context, victim);
    }
}
