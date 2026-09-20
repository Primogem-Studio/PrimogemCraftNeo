package net.per.primogemcraft.collab.teyvatdelight;

import com.guoche.teyvatdelight.TeyvatDelight;
import com.guoche.teyvatdelight.TeyvatItemData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.registry.PGCItems;

final class TeyvatDelightBridge {
    private TeyvatDelightBridge() {
    }

    static void register(IEventBus modBus) {
        modBus.addListener(TeyvatDelightBridge::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(TeyvatDelightBridge::addFoodTooltip);
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        for (var holder : PGCItems.REGISTRY.getEntries()) {
            var item = holder.get();
            if (!isFood(item)) continue;
            var stars = item == PGCItems.ADEPTUS_TEMPTATION.get() ? 5
                    : switch (item.components().getOrDefault(DataComponents.RARITY, Rarity.COMMON)) {
                        case COMMON -> 1;
                        case UNCOMMON -> 2;
                        case RARE -> 3;
                        case EPIC -> 4;
                    };
            event.modify(item, builder -> builder
                    .set(TeyvatDelight.STARS.get(), stars)
                    .set(TeyvatDelight.FOOD_QUALITY.get(), TeyvatItemData.QUALITY_NORMAL));
        }
    }

    private static void addFoodTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(PrimogemCraft.MOD_ID)
                || !isFood(stack.getItem())) return;
        var tooltip = event.getToolTip();
        var stars = TeyvatItemData.getStars(stack);
        if (stars > 0 && !tooltip.isEmpty())
            tooltip.set(0, tooltip.getFirst().copy().withStyle(TeyvatItemData.getStarColor(stars)));
        TeyvatItemData.appendRarityTooltip(stack, tooltip);
        TeyvatItemData.appendFoodQualityTooltip(stack, tooltip);
    }

    private static boolean isFood(Item item) {
        return item.components().has(DataComponents.FOOD)
                || item == PGCItems.ORDINARY_COFFEE.get()
                || item == PGCItems.A_CAKE_FOR_YOU.get();
    }
}
