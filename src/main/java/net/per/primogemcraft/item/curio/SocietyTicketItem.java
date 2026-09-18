package net.per.primogemcraft.item.curio;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;

public class SocietyTicketItem extends CurioItem {
    private static final double CHANCE = 0.05D;

    public SocietyTicketItem(Properties properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, properties);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.KILL) return;
        if (!(impact.subject() instanceof LivingEntity victim)) return;
        if (!victim.getType().is(EntityTypeTags.UNDEAD) && !victim.getType().is(EntityTypeTags.ARTHROPOD)) return;
        if (!context.chance(CHANCE)) return;
        CurioLoot.spawn(impact.level(), victim.position(), new ItemStack(PGCItems.COSMIC_FRAGMENT.get()));
    }
}
