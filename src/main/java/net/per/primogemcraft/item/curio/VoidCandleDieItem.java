package net.per.primogemcraft.item.curio;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.curio.CurioEnchanting;
import net.per.primogemcraft.system.curio.CurioForm;
import net.per.primogemcraft.system.curio.EnchantingCurioItem;

public class VoidCandleDieItem extends EnchantingCurioItem {
    public VoidCandleDieItem(CurioForm form, int integrity, Spec spec, Properties properties) {
        super(form, integrity, spec, properties);
    }

    @Override
    protected Roll roll(ServerPlayer player, ItemStack target) {
        var picked = CurioEnchanting.inventoryEnchantment(player, target);
        if (picked.isEmpty()) return super.roll(player, target);
        var enchantment = picked.get();
        return new Roll(CurioEnchanting.withEnchantment(target, enchantment), enchantment.level);
    }
}
