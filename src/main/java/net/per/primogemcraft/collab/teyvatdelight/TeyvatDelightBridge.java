package net.per.primogemcraft.collab.teyvatdelight;

import net.minecraft.registry.Registries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.registry.PGCItems;

/**
 * 提瓦特佳肴联动桥接（Fabric）。
 * <p>通过反射调用 TeyvatItemData API，避免硬依赖。
 */
final class TeyvatDelightBridge {
    private TeyvatDelightBridge() {
    }

    static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (!Registries.ITEM.getId(stack.getItem()).getNamespace().equals(PrimogemCraft.MOD_ID)
                    || !isFood(stack.getItem())) return;
            applyDefaults(stack);
            appendTooltips(stack, lines);
        });
    }

    private static void applyDefaults(ItemStack stack) {
        try {
            var cls = Class.forName("com.guoche.teyvatdelight.api.TeyvatItemData");
            var hasStars = cls.getMethod("hasStars", ItemStack.class);
            if (!(boolean) hasStars.invoke(null, stack)) {
                var item = stack.getItem();
                var stars = item == PGCItems.ADEPTUS_TEMPTATION.get() ? 5 : switch (rarityOf(item)) {
                    case COMMON -> 1;
                    case UNCOMMON -> 2;
                    case RARE -> 3;
                    case EPIC -> 4;
                };
                cls.getMethod("setStars", ItemStack.class, int.class).invoke(null, stack, stars);
            }
            var hasQuality = cls.getMethod("hasFoodQuality", ItemStack.class);
            if (!(boolean) hasQuality.invoke(null, stack)) {
                var normal = cls.getField("QUALITY_NORMAL").get(null);
                cls.getMethod("setFoodQuality", ItemStack.class, int.class).invoke(null, stack, normal);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void appendTooltips(ItemStack stack, java.util.List<net.minecraft.text.Text> lines) {
        try {
            var cls = Class.forName("com.guoche.teyvatdelight.api.TeyvatItemData");
            var stars = (int) cls.getMethod("getStars", ItemStack.class).invoke(null, stack);
            if (stars > 0 && !lines.isEmpty()) {
                var color = cls.getMethod("getStarColor", int.class).invoke(null, stars);
                var first = lines.get(0).copy();
                first.getStyle().withColor((net.minecraft.text.TextColor) color);
                lines.set(0, first);
            }
            cls.getMethod("appendRarityTooltip", ItemStack.class, java.util.List.class).invoke(null, stack, lines);
            cls.getMethod("appendFoodQualityTooltip", ItemStack.class, java.util.List.class).invoke(null, stack, lines);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Rarity rarityOf(Item item) {
        return item.getRarity(new ItemStack(item));
    }

    private static boolean isFood(Item item) {
        return item.isFood()
                || item == PGCItems.ORDINARY_COFFEE.get()
                || item == PGCItems.A_CAKE_FOR_YOU.get();
    }
}
