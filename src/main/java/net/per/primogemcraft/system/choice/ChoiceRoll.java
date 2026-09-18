package net.per.primogemcraft.system.choice;

import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface ChoiceRoll {
    List<ItemStack> roll();
}
