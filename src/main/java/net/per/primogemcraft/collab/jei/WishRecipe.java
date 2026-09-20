package net.per.primogemcraft.collab.jei;

import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.system.wish.WishRarity;

public record WishRecipe(WishRarity rarity, ItemStack output) {
}
