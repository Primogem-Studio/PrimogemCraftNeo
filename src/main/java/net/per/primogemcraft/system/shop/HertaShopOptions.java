package net.per.primogemcraft.system.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.per.primogemcraft.registry.PGCItems;
import net.per.primogemcraft.system.choice.*;

import java.util.List;
import java.util.function.IntConsumer;

public final class HertaShopOptions {
    public static final int CURIO = 0;
    public static final int CODE = 1;
    public static final int ENCHANT = 2;
    public static final int LEAVE = 3;

    private static final String TITLE_KEY = "gui.primogemcraft.herta_shop.title";
    private static final String HINT_KEY = "gui.primogemcraft.herta_shop.hint";
    private static final String CURIO_KEY = "gui.primogemcraft.herta_shop.curio";
    private static final String CURIO_DETAIL_KEY = "gui.primogemcraft.herta_shop.curio.detail";
    private static final String CODE_KEY = "gui.primogemcraft.herta_shop.code";
    private static final String CODE_DETAIL_KEY = "gui.primogemcraft.herta_shop.code.detail";
    private static final String ENCHANT_KEY = "gui.primogemcraft.herta_shop.enchant";
    private static final String ENCHANT_DETAIL_KEY = "gui.primogemcraft.herta_shop.enchant.detail";
    private static final String LEAVE_KEY = "gui.primogemcraft.herta_shop.leave";
    private static final String LEAVE_DETAIL_KEY = "gui.primogemcraft.herta_shop.leave.detail";
    private static final int FULL_TURN = 1;

    private HertaShopOptions() {
    }

    public static void open(ServerPlayer player, Component curioPrice, Component codePrice, Component enchantPrice, IntConsumer onOption) {
        var cards = List.of(
                card(CURIO_KEY, new ItemStack(PGCItems.FRUIT_OF_THE_ALIEN_TREE.get()), CURIO_DETAIL_KEY, curioPrice),
                card(CODE_KEY, new ItemStack(PGCItems.RANDOM_EVENT_ERROR_CODE.get()), CODE_DETAIL_KEY, codePrice),
                card(ENCHANT_KEY, new ItemStack(Items.ENCHANTING_TABLE), ENCHANT_DETAIL_KEY, enchantPrice),
                card(LEAVE_KEY, new ItemStack(Items.LEATHER_BOOTS), LEAVE_DETAIL_KEY, Component.empty()));
        ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.ENCHANT_CARDS, ChoiceSupport.spin(FULL_TURN, ChoiceSpinSpeed.FASTEST), ChoiceRegistry.NO_SETTLE_TICKS, cards, index -> {
            onOption.accept(index);
            return true;
        });
    }

    private static ChoiceCard card(String titleKey, ItemStack icon, String detailKey, Component price) {
        return ChoiceSupport.card(Component.translatable(titleKey), icon, Component.translatable(detailKey), price).withTextTooltip().withQuality(ChoiceCard.SILENT_QUALITY);
    }
}
