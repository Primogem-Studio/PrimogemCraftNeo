package net.per.primogemcraft.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.per.primogemcraft.system.weapon.WishWeapon;

public class WeaponStacksDecorator implements IItemDecorator {
    private static final int COLOR = 0xFFAA00;

    @Override
    public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof WishWeapon weapon)) return false;
        var stacks = weapon.stacks(stack);
        if (stacks.permanent() || !stacks.visible()) return false;
        ItemBar.render(graphics, x, y, ItemBar.STACKS_TOP, ItemBar.filled(stacks.value(), stacks.capacity()), COLOR);
        return true;
    }
}
