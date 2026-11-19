package net.per.primogemcraft.collab.teyvatdelight;

import com.guoche.teyvatdelight.api.TeyvatItemData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.per.primogemcraft.PrimogemCraft;
import net.per.primogemcraft.registry.PGCItems;

final class TeyvatDelightBridge {
    private TeyvatDelightBridge() {
    }

    static void register(IEventBus modBus) {
        MinecraftForge.EVENT_BUS.addListener(TeyvatDelightBridge::addFoodTooltip);
        MinecraftForge.EVENT_BUS.addListener(TeyvatDelightBridge::addReloadListeners);
    }

    private static void addReloadListeners(net.minecraftforge.event.AddReloadListenerEvent event) {
        event.addListener(new CurrencyExchange());
    }

    /**
     * 按需为本模组食物写入星级与品质。
     *
     * <p>1.21 用 {@code ModifyDefaultComponentsEvent} 在注册期设默认组件；1.20.1 无此机制，
     * 改为在渲染提示前确保 NBT 已写入，效果等价（玩家看到的星级与品质一致）。
     */
    private static void applyDefaults(net.minecraft.world.item.ItemStack stack) {
        var item = stack.getItem();
        if (!isFood(item)) return;
        if (!TeyvatItemData.hasStars(stack)) {
            var stars = item == PGCItems.ADEPTUS_TEMPTATION.get() ? 5 : switch (rarityOf(item)) {
                case COMMON -> 1;
                case UNCOMMON -> 2;
                case RARE -> 3;
                case EPIC -> 4;
            };
            TeyvatItemData.setStars(stack, stars);
        }
        if (!TeyvatItemData.hasFoodQuality(stack)) {
            TeyvatItemData.setFoodQuality(stack, TeyvatItemData.QUALITY_NORMAL);
        }
    }

    /** 1.20.1 的稀有度由 Item.getRarity(ItemStack) 提供，而非数据组件。 */
    private static Rarity rarityOf(Item item) {
        return item.getRarity(new net.minecraft.world.item.ItemStack(item));
    }

    private static void addFoodTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(PrimogemCraft.MOD_ID)
                || !isFood(stack.getItem())) return;
        applyDefaults(stack);
        var tooltip = event.getToolTip();
        var stars = TeyvatItemData.getStars(stack);
        if (stars > 0 && !tooltip.isEmpty())
            tooltip.set(0, tooltip.get(0).copy().withStyle(TeyvatItemData.getStarColor(stars)));
        TeyvatItemData.appendRarityTooltip(stack, tooltip);
        TeyvatItemData.appendFoodQualityTooltip(stack, tooltip);
    }

    private static boolean isFood(Item item) {
        // 1.20.1 用 Item.isEdible() 判定食物，而非 DataComponents.FOOD
        return item.isEdible()
                || item == PGCItems.ORDINARY_COFFEE.get()
                || item == PGCItems.A_CAKE_FOR_YOU.get();
    }
}
