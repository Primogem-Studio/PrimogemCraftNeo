package net.per.primogemcraft.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class ItemActivationFx {
    private ItemActivationFx() {
    }

    public static void show(ItemStack stack) {
        if (stack.isEmpty()) return;
        Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
    }
}
