package net.per.primogemcraft.item.misc;

import net.minecraft.world.entity.player.Player;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;

public class ViolaneItem extends DescribedItem {
    public ViolaneItem(Properties properties) {
        super(properties);
    }

    public static boolean isActive(Player player) {
        var inventory = player.getInventory();
        for (var slot = 0; slot < inventory.getContainerSize(); slot++)
            if (inventory.getItem(slot).is(PGCItems.VIOLANE.get())) return true;
        for (var stack : CuriosIntegration.equipped(player))
            if (stack.is(PGCItems.VIOLANE.get())) return true;
        return false;
    }
}
