package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.client.WishLootItems;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.wish.WishRarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WishDisplayGenerator implements DynamicDisplayGenerator<WishDisplay> {
    @Override
    public Optional<List<WishDisplay>> getRecipeFor(EntryStack<?> entry) {
        if (!entry.getType().equals(VanillaEntryTypes.ITEM)) return Optional.empty();
        var stack = entry.<ItemStack>castValue();
        if (stack.isEmpty()) return Optional.empty();
        var displays = new ArrayList<WishDisplay>();
        for (var rarity : WishRarity.values()) {
            if (WishLootItems.of(rarity).contains(stack.getItem())) {
                displays.add(new WishDisplay(rarity, List.of(EntryIngredients.of(stack.getItem()))));
            }
        }
        return Optional.of(displays);
    }

    @Override
    public Optional<List<WishDisplay>> getUsageFor(EntryStack<?> entry) {
        if (!entry.getType().equals(VanillaEntryTypes.ITEM)) return Optional.empty();
        var stack = entry.<ItemStack>castValue();
        if (stack.is(PGCItems.ACQUAINT_FATE.get()) || stack.is(PGCItems.INTERTWINED_FATE.get())) {
            return Optional.of(displays());
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<WishDisplay>> generate(ViewSearchBuilder builder) {
        if (!builder.getCategories().contains(PGCREIPlugin.WISH)) return Optional.empty();
        if (!builder.getUsagesFor().isEmpty() || !builder.getRecipesFor().isEmpty()) return Optional.empty();
        return Optional.of(displays());
    }

    private static List<WishDisplay> displays() {
        var displays = new ArrayList<WishDisplay>();
        for (var rarity : WishRarity.values()) {
            for (var item : WishLootItems.of(rarity)) {
                displays.add(new WishDisplay(rarity, List.of(EntryIngredients.of(item))));
            }
        }
        return displays;
    }
}
