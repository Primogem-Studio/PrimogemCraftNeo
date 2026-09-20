package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

public class WishMaterialDisplay extends BasicDisplay {
    public static final int COLUMNS = 9;
    public static final int ROWS = 5;
    public static final int PAGE_SIZE = COLUMNS * ROWS;

    private final List<Integer> values;

    public WishMaterialDisplay(List<EntryIngredient> inputs, List<Integer> materialValues) {
        super(List.copyOf(inputs), List.of());
        values = List.copyOf(materialValues);
    }

    public int value(int index) {
        return values.get(index);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return PGCREIPlugin.WISH_MATERIALS;
    }
}
