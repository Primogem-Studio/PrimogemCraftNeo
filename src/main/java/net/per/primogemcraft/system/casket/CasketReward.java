package net.per.primogemcraft.system.casket;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.system.choice.ChoiceCard;
import net.per.primogemcraft.system.choice.ChoiceCardSpin;
import net.per.primogemcraft.system.choice.ChoiceMode;
import net.per.primogemcraft.system.choice.ChoiceRegistry;
import net.per.primogemcraft.system.choice.ChoiceSpinSpeed;
import net.per.primogemcraft.system.choice.ChoiceSupport;
import net.per.primogemcraft.system.choice.ChoiceVisual;

import java.util.ArrayList;
import java.util.List;

public final class CasketReward {
    private static final String TITLE_KEY = "gui.primogemcraft.casket_reward.title";
    private static final String HINT_KEY = "gui.primogemcraft.casket_reward.hint";
    private static final ChoiceCardSpin SPIN = ChoiceCardSpin.of(1, ChoiceSpinSpeed.BRISK);

    private CasketReward() {
    }

    public static void open(ServerPlayer player, List<ItemStack> rewards) {
        var shown = new ArrayList<ItemStack>();
        for (var reward : rewards) if (!reward.isEmpty()) shown.add(reward);
        if (shown.isEmpty()) return;
        var cards = new ArrayList<ChoiceCard>();
        for (var reward : shown) cards.add(ChoiceSupport.card(reward));
        ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.DEFAULT_CARDS, ChoiceSupport.spin(SPIN), ChoiceRegistry.NO_SETTLE_TICKS, ChoiceMode.REVEAL, cards, index -> {
            for (var reward : shown) OtherworldBankbook.give(player, reward);
            return true;
        });
    }
}
