package net.per.primogemcraft.item.curio;

import net.minecraft.text.Text;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.per.primogemcraft.registry.PGCSounds;
import net.per.primogemcraft.system.curio.CurioContext;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.CurioItem;
import net.per.primogemcraft.system.curio.CurioTrigger;
import net.per.primogemcraft.system.weapon.WeaponAttributes;
import net.per.primogemcraft.system.weapon.WeaponEnhancement;
import net.per.primogemcraft.system.weapon.WishWeapon;

public class TriangularDrumRollDeviceItem extends CurioItem {
    private static final String NOT_WEAPON_KEY = "message.primogemcraft.curio.drum_roll_device.not_weapon";
    private static final String CAPPED_KEY = "message.primogemcraft.curio.drum_roll_device.capped";
    private static final int LEVEL_LIMIT = 60;
    private static final int MIN_LEVELS = 1;
    private static final int MAX_LEVELS = 8;

    public TriangularDrumRollDeviceItem(Settings properties) {
        super(CurioTrigger.RIGHT_CLICK, CurioForm.NORMAL, properties.fireproof());
    }

    @Override
    public void activated(CurioContext context) {
        var player = context.player();
        var weapon = player.getOffHandStack();
        if (!(weapon.getItem() instanceof WishWeapon)) {
            context.announce(Text.translatable(NOT_WEAPON_KEY));
            return;
        }
        if (!WeaponEnhancement.grantLevels(weapon, MathHelper.nextInt(context.random(), MIN_LEVELS, MAX_LEVELS), LEVEL_LIMIT)) {
            context.announce(Text.translatable(CAPPED_KEY));
            return;
        }
        WeaponAttributes.refreshLevel(weapon, player);
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), PGCSounds.ENHANCEMENT_SUCCESS.get(), SoundCategory.PLAYERS, 1.0F, 1.0F);
        context.stack().decrement(1);
    }
}
