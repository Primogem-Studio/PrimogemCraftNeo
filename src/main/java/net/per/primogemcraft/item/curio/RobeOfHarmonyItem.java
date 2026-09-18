package net.per.primogemcraft.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.per.primogemcraft.system.curio.*;

public class RobeOfHarmonyItem extends CurioItem {
    public RobeOfHarmonyItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.FUSION, properties.fireResistant());
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.ATTACK) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        ExtraMagicDamage.strike(context, victim);
    }
}
