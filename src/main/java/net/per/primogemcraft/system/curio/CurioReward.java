package net.per.primogemcraft.system.curio;

import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.per.primogemcraft.item.misc.OtherworldBankbook;
import net.per.primogemcraft.system.choice.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CurioReward {
    private static final String TITLE_KEY = "gui.primogemcraft.curio_reward.title";
    private static final String HINT_KEY = "gui.primogemcraft.curio_reward.hint";
    private static final ChoiceCardSpin SPIN = ChoiceCardSpin.of(1, ChoiceSpinSpeed.BRISK);
    private static final Map<UUID, Deque<Reveal>> WAITING = new HashMap<>();

    private CurioReward() {
    }

    public static void open(ServerPlayerEntity player, ChoiceCardTextures textures, List<ItemStack> rewards) {
        var shown = new ArrayList<ItemStack>();
        for (var reward : rewards) if (!reward.isEmpty()) shown.add(reward);
        if (shown.isEmpty()) return;
        var reveal = new Reveal(textures, List.copyOf(shown));
        if (ChoiceRegistry.isPending(player)) {
            WAITING.computeIfAbsent(player.getUuid(), key -> new ArrayDeque<>()).addLast(reveal);
            return;
        }
        show(player, reveal);
    }

    public static void tick(ServerPlayerEntity player) {
        var queue = WAITING.get(player.getUuid());
        if (queue == null || queue.isEmpty() || ChoiceRegistry.isPending(player)) return;
        var reveal = queue.pollFirst();
        if (queue.isEmpty()) WAITING.remove(player.getUuid(), queue);
        show(player, reveal);
    }

    public static void clear(ServerPlayerEntity player) {
        var queue = WAITING.remove(player.getUuid());
        if (queue == null) return;
        for (var reveal : queue) grant(player, reveal.rewards());
    }

    private static void show(ServerPlayerEntity player, Reveal reveal) {
        var cards = new ArrayList<ChoiceCard>();
        for (var reward : reveal.rewards()) cards.add(CurioChoice.presentation(reward));
        ChoiceRegistry.open(player, Text.translatable(TITLE_KEY), Text.translatable(HINT_KEY), ChoiceVisual.ITEM_MODEL, ChoiceSupport.BACKGROUND, reveal.textures(), ChoiceSupport.spin(SPIN), ChoiceRegistry.NO_SETTLE_TICKS, ChoiceMode.REVEAL, cards, index -> {
            grant(player, reveal.rewards());
            return true;
        });
    }

    private static void grant(ServerPlayerEntity player, List<ItemStack> rewards) {
        for (var reward : rewards) {
            if (OtherworldBankbook.isFragment(reward)) {
                OtherworldBankbook.give(player, reward);
                continue;
            }
            player.getInventory().insertStack(reward);
            if (reward.isEmpty()) continue;
            if (Curios.equip(player, reward)) continue;
            Curios.give(player, reward);
        }
    }

    private record Reveal(ChoiceCardTextures textures, List<ItemStack> rewards) {
    }
}
