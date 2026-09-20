package net.per.primogemcraft.collab.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record WishMaterialRecipe(List<ItemStack> inputs, List<Integer> values) {
    public static final int COLUMNS = 9;
    public static final int ROWS = 5;
    public static final int PAGE_SIZE = COLUMNS * ROWS;

    public WishMaterialRecipe {
        inputs = List.copyOf(inputs);
        values = List.copyOf(values);
    }
}
