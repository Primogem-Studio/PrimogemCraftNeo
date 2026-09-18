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
import net.per.primogemcraft.system.weapon.WeaponState;
import net.per.primogemcraft.system.weapon.WishWeapon;

public class JubilantDrumRollDeviceItem extends CurioItem {
    private static final String REFUSED_KEY = "message.primogemcraft.curio.jubilant_drum_roll_device.refused";
    private static final String CAPPED_KEY = "message.primogemcraft.curio.drum_roll_device.capped";
    private static final double SURGE_CHANCE = 0.1D;
    private static final int SURGE_LEVELS = 30;
    private static final int RUINED_LEVEL = -999;

    public JubilantDrumRollDeviceItem(Properties properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.FUSION, properties);
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var weapon = player.getOffhandItem();
        if (!(weapon.getItem() instanceof WishWeapon)) {
            context.announce(Component.translatable(REFUSED_KEY));
            return;
        }
        if (context.chance(SURGE_CHANCE)) {
            var state = WeaponState.of(weapon);
            if (WeaponEnhancement.grantLevels(weapon, SURGE_LEVELS, WeaponEnhancement.maxLevel(state.refinements()))) {
                WeaponAttributes.refreshLevel(weapon, player);
            } else {
                context.announce(Component.translatable(CAPPED_KEY));
            }
        } else {
            WeaponEnhancement.setLevel(weapon, RUINED_LEVEL);
            WeaponAttributes.refreshLevel(weapon, player);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.ENHANCEMENT_SUCCESS.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        context.stack().shrink(1);
    }
}
