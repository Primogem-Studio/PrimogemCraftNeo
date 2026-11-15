package net.per.primogemcraft.item.curio;

import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;

public class SocietyTicketItem extends CurioItem {
    private static final double CHANCE = 0.05D;

    public SocietyTicketItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.KILL) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (victim.getGroup() != net.minecraft.entity.EntityGroup.UNDEAD && victim.getGroup() != net.minecraft.entity.EntityGroup.ARTHROPOD) return;
        if (!context.chance(CHANCE)) return;
        CurioLoot.spawn(impact.level(), victim.getPos(), new ItemStack(PGCItems.COSMIC_FRAGMENT.get()));
    }
}
