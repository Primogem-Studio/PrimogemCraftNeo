package net.per.primogemcraft.item.curio;

import net.minecraft.text.Text;
import net.minecraft.entity.ExperienceOrbEntity;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.*;
import net.per.primogemcraft.util.PlayerItems;

public class TypicalGeniusSocietyGossipItem extends CurioItem {
    private static final String CONVERTED_KEY = "message.primogemcraft.curio.typical_genius_society_gossip.converted";
    private static final int CONVERSION_LIMIT = 256;
    private static final int XP_PER_FRAGMENT = 8;
    private static final float LOSS = 0.5F;

    public TypicalGeniusSocietyGossipItem(Settings properties) {
        super(CurioTrigger.ACTIVE, CurioForm.NORMAL, CONVERSION_LIMIT, properties);
    }

    @Override
    public void presence(CurioContext context) {
        var player = context.player();
        var fragments = PlayerItems.count(player, PGCItems.COSMIC_FRAGMENT.get());
        if (fragments <= 0) return;
        var converted = Math.min(fragments, context.remaining());
        if (converted <= 0) return;
        PlayerItems.take(player, PGCItems.COSMIC_FRAGMENT.get(), converted);
        player.addExperience(converted * XP_PER_FRAGMENT);
        context.announce(Text.translatable(CONVERTED_KEY, context.stack().getName()));
        context.damage(converted);
    }

    @Override
    public void impacted(CurioContext context, CurioImpact impact) {
        if (impact.signal() != CurioSignal.XP_PICKED) return;
        if (!(impact.subject() instanceof ExperienceOrbEntity orb)) return;
        context.player().addExperience(orb.getExperienceAmount());
    }

    @Override
    public void broken(CurioContext context) {
        var player = context.player();
        player.addExperience(-(int) (player.totalExperience * LOSS));
    }
}
