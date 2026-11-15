package net.per.primogemcraft.collab.rei;

import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.client.WishMaterialValues;
import net.per.primogemcraft.registry.PGCItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WishMaterialDisplayGenerator implements DynamicDisplayGenerator<WishMaterialDisplay> {
    @Override
    public Optional<List<WishMaterialDisplay>> getUsageFor(EntryStack<?> entry) {
        if (!entry.getType().equals(VanillaEntryTypes.ITEM)) return Optional.empty();
        var stack = entry.<ItemStack>castValue();
        if (stack.is(PGCItems.WISH_CORE.get())) return Optional.of(pages());
        var value = WishMaterialValues.get().get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (value == null) return Optional.empty();
        return Optional.of(List.of(new WishMaterialDisplay(List.of(EntryIngredients.of(stack.getItem())), List.of(value))));
    }

    @Override
    public Optional<List<WishMaterialDisplay>> generate(ViewSearchBuilder builder) {
        if (!builder.getCategories().contains(PGCREIPlugin.WISH_MATERIALS)) return Optional.empty();
        if (!builder.getUsagesFor().isEmpty() || !builder.getRecipesFor().isEmpty()) return Optional.empty();
        return Optional.of(pages());
    }

    private static List<WishMaterialDisplay> pages() {
        var entries = WishMaterialValues.get().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        var displays = new ArrayList<WishMaterialDisplay>();
        var inputs = new ArrayList<EntryIngredient>();
        var values = new ArrayList<Integer>();
        for (var entry : entries) {
            var item = BuiltInRegistries.ITEM.getOptional(entry.getKey());
            if (item.isEmpty() || entry.getValue() <= 1) continue;
            inputs.add(EntryIngredients.of(item.get()));
            values.add(entry.getValue());
            if (inputs.size() == WishMaterialDisplay.PAGE_SIZE) {
                displays.add(new WishMaterialDisplay(inputs, values));
                inputs.clear();
                values.clear();
            }
        }
        if (!inputs.isEmpty()) displays.add(new WishMaterialDisplay(inputs, values));
        return displays;
    }
}
