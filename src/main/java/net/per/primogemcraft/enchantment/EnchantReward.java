package net.per.primogemcraft.enchantment;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.per.primogemcraft.system.choice.*;
import net.per.primogemcraft.system.curio.CurioEnchanting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EnchantReward {
    private static final String TITLE_KEY = "gui.primogemcraft.enchant_reward.title";
    private static final String HINT_KEY = "gui.primogemcraft.enchant_reward.hint";
    private static final ChoiceCardSpin SPIN = ChoiceCardSpin.of(1, ChoiceSpinSpeed.BRISK);
    private static final Map<UUID, Deque<List<EnchantOption>>> WAITING = new HashMap<>();

    private EnchantReward() {
    }

    public static void open(ServerPlayer player, List<EnchantOption> options) {
        var shown = applicable(options);
        if (shown.isEmpty()) return;
        if (ChoiceRegistry.isPending(player)) {
            WAITING.computeIfAbsent(player.getUUID(), key -> new ArrayDeque<>()).addLast(shown);
            return;
        }
        show(player, shown);
    }

    public static void tick(ServerPlayer player) {
        var queue = WAITING.get(player.getUUID());
        if (queue == null || queue.isEmpty() || ChoiceRegistry.isPending(player)) return;
        var shown = queue.pollFirst();
        if (queue.isEmpty()) WAITING.remove(player.getUUID(), queue);
        show(player, shown);
    }

    public static void clear(ServerPlayer player) {
        var queue = WAITING.remove(player.getUUID());
        if (queue == null) return;
        for (var shown : queue) grant(shown);
    }

    private static void show(ServerPlayer player, List<EnchantOption> shown) {
        var cards = new ArrayList<ChoiceCard>();
        for (var option : shown) cards.add(EnchantChoice.presentation(option));
        ChoiceRegistry.open(player, Component.translatable(TITLE_KEY), Component.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, ChoiceSupport.ENCHANT_CARDS, ChoiceSupport.spin(SPIN), ChoiceRegistry.NO_SETTLE_TICKS, ChoiceMode.REVEAL, cards, index -> {
            grant(shown);
            return true;
        });
    }

    private static void grant(List<EnchantOption> options) {
        for (var option : options) CurioEnchanting.mergeResult(option.target(), option.preview());
    }

    private static List<EnchantOption> applicable(List<EnchantOption> options) {
        var shown = new ArrayList<EnchantOption>();
        for (var option : options) {
            var preview = option.preview();
            if (preview.isEmpty() || EnchantmentHelper.getEnchantmentsForCrafting(preview).isEmpty()) continue;
            shown.add(option);
        }
        return shown;
    }
}
