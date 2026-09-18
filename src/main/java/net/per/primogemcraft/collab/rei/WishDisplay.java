package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.per.primogemcraft.system.wish.WishRarity;

import java.util.List;

public class WishDisplay extends BasicDisplay {
    private static final String RARITY_TAG = "rarity";

    private final WishRarity rarity;

    public WishDisplay(WishRarity rarity, List<EntryIngredient> outputs) {
        super(List.of(), outputs);
        this.rarity = rarity;
    }

    public WishRarity rarity() {
        return rarity;
    }

    public static BasicDisplay.Serializer<WishDisplay> serializer() {
        return Serializer.ofRecipeLess(
                (inputs, outputs, tag) -> new WishDisplay(PGCREIPlugin.rarityOf(tag.getString(RARITY_TAG)), outputs),
                WishDisplay::write);
    }

    private static void write(WishDisplay display, CompoundTag tag) {
        tag.putString(RARITY_TAG, display.rarity.name());
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return PGCREIPlugin.WISH;
    }
}
