package net.per.primogemcraft.item.curio;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.weapon.WeaponAttributes;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;

public class SpaceTimeTriangleItem extends CurioItem {
    private static final String REFUSED_KEY = "message.primogemcraft.curio.space_time_triangle.refused";
    private static final double REFINE_CHANCE = 0.5D;

    public SpaceTimeTriangleItem(Properties properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.FUSION, properties);
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var weapon = player.getOffhandItem();
        if (WeaponEnhancement.canGrantRefinement(weapon, player)) {
            context.announce(Component.translatable(REFUSED_KEY));
            return;
        }
        if (context.chance(REFINE_CHANCE) && WeaponEnhancement.grantRefinement(weapon, player)) {
            WeaponAttributes.refreshLevel(weapon, player);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.ENHANCEMENT_SUCCESS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        context.stack().shrink(1);
    }
}
