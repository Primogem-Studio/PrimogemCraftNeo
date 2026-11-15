package net.per.primogemcraft.item.misc;

import net.minecraft.entity.player.PlayerEntity;
import net.per.primogemcraft.item.tool.DescribedItem;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.curio.compat.CuriosIntegration;

public class ViolaneItem extends DescribedItem {
    public ViolaneItem(Settings properties) {
        super(properties);
    }

    public static boolean isActive(PlayerEntity player) {
        var inventory = player.getInventory();
        for (var slot = 0; slot < inventory.size(); slot++)
            if (inventory.getStack(slot).isOf(PGCItems.VIOLANE.get())) return true;
        for (var stack : CuriosIntegration.equipped(player))
            if (stack.isOf(PGCItems.VIOLANE.get())) return true;
        return false;
    }
}
